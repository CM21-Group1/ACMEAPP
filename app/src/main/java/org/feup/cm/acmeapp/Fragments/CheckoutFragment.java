package org.feup.cm.acmeapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.feup.cm.acmeapp.model.Voucher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CheckoutFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private List<Product> productList;
    private String userId;
    private double totalAmount = 0;
    private int totalNumProds = 0;
    private List<Voucher> voucherList = new ArrayList<>();
    private List<String> voucherItems = new ArrayList<>();
    private String voucherIdChosen = null;
    private TextView totalWithVoucher;
    private TextView totalWithVoucherLabel;

    private Voucher selectedVoucher;

    private final double percentage = 0.15;
    private double valueToSubtract = 0;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.checkout_fragment, container, false);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        productList = sharedViewModel.getProductList();

        for (Product product : productList) {
            totalAmount += product.getPrice();
        }

        totalNumProds = productList.size();

        TextView txt_total = root.findViewById(R.id.totalPrice);
        txt_total.setText(totalAmount + "€");
        TextView txt_num = root.findViewById(R.id.numberOfProducts);
        txt_num.setText(String.valueOf(totalNumProds));

        totalWithVoucher = root.findViewById(R.id.totalPriceWithVoucher);
        totalWithVoucherLabel = root.findViewById(R.id.totalPriceWithVoucherLabel);
        totalWithVoucher.setVisibility(View.INVISIBLE);
        totalWithVoucherLabel.setVisibility(View.INVISIBLE);

        Button voucher_dialog = root.findViewById(R.id.voucher_dialog);
        voucher_dialog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                List<String> voucherNames = new ArrayList<>();
                for (Voucher voucherTemp : voucherList) {
                    voucherNames.add(voucherTemp.get_id());
                }
                CharSequence[] simpleArray = voucherNames.toArray(new CharSequence[voucherNames.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Vouchers")
                        .setSingleChoiceItems(simpleArray, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selectedVoucher = voucherList.get(which);
                                        System.out.println(selectedVoucher.get_id());
                                    }
                                })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println(selectedVoucher.get_id());
                                valueToSubtract = totalAmount * percentage;
                                totalAmount -= valueToSubtract;
                                totalWithVoucher.setText(totalAmount + "€");
                                totalWithVoucher.setVisibility(View.VISIBLE);
                                totalWithVoucherLabel.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                selectedVoucher = null;
                                totalAmount += valueToSubtract;
                                totalWithVoucher.setText(totalAmount + "€");
                                totalWithVoucher.setVisibility(View.INVISIBLE);
                                totalWithVoucherLabel.setVisibility(View.INVISIBLE);
                            }
                        });

                builder.show();
            }
        });

        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        userId = settings.getString(Constants.PREF_USERID, "");

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Navigation.findNavController(root).navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        Button pay_btn = root.findViewById(R.id.pay_btn);
        pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR Code Scan

                if(selectedVoucher == null){
                    sharedViewModel.setPurchase(new Purchase(userId, productList, totalAmount));
                }else{
                    sharedViewModel.setPurchase(new Purchase(userId, productList, totalAmount, selectedVoucher));
                }
                Navigation.findNavController(root).navigate(R.id.action_checkoutFragment_to_QRCheckoutFragment);
            }
        });

        new APIRequest().execute();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private class APIRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.baseUrl + userId);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream;

                if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = urlConnection.getInputStream();
                } else {
                    inputStream = urlConnection.getErrorStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp, response = "";
                while ((temp = bufferedReader.readLine()) != null) {
                    response += temp;
                }
                return response;
            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            try {
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject purchaseJson = jsonArray.getJSONObject(i);

                    Voucher voucherTemp = new Voucher();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    Date date = format.parse(purchaseJson.get("createdAt").toString());
                    voucherTemp.setUserId(purchaseJson.get("userId").toString());
                    voucherTemp.setCreatedAt(date);
                    voucherTemp.set_id(purchaseJson.get("_id").toString());

                    voucherList.add(voucherTemp);
                    voucherItems.add("Voucher #" + i + " with 15%");
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }
    }

}

