package org.feup.cm.acmeapp.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.ProductsDialog;
import org.feup.cm.acmeapp.R;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private BottomNavigationView bottomNavigation;
    private ListView list;
    private List<Purchase> purchaseList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private String userId;

    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.home_fragment, container, false);

        bottomNavigation = root.findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.home);

        if (!isOnline()) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Info");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage("Internet not available, Cross check your internet connectivity and try again");
                builder.setCancelable(false);
                builder.setPositiveButton("Retry", null);

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                if (isOnline()) {
                                    new APIRequest().execute();
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });

                dialog.show();
            } catch (Exception e) {
                System.out.println();
            }
        }

        setHasOptionsMenu(true);
        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        System.out.println("HOME FRAGMENT");
                        return true;
                    case R.id.vouchers:
                        Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_vouchersFragment);
                        return true;
                    case R.id.shopping_cart:
                        Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_shoppingCartFragment);
                        return true;
                }
                return false;
            }
        });

        // USER ID RETRIEVED FROM SHAREDPREFERENCES
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        userId = settings.getString(Constants.PREF_USERID, "");
        //System.out.println(userId);

        FirebaseMessaging.getInstance().subscribeToTopic(userId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "MESSAGE";
                        if (!task.isSuccessful()) {
                            msg = "NOT MESSAGE";
                        }
                        //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                });

        list = root.findViewById(R.id.purchases_listview);
        list.setEmptyView(root.findViewById(R.id.empty_list));
        adapter = new CustomArrayAdapter(getContext(), 0, purchaseList);

        // Get all purchases from customer from user id
        new APIRequest().execute();

        //On clicked item from list of purchases
        //Open a dialog with all products from a purchase
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                // Create a dialog for each purchase clicked. Dialog with all products listed
                Purchase purchaseClicked = purchaseList.get(position);

                if(purchaseClicked.getVoucher() != null){
                    ProductsDialog cdd = new ProductsDialog(getActivity(), purchaseClicked.getProducts(), purchaseClicked.getVoucher());
                    cdd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    cdd.show();
                }else{
                    ProductsDialog cdd = new ProductsDialog(getActivity(), purchaseClicked.getProducts());
                    cdd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    cdd.show();
                }
            }
        });

        return root;
    }

    public boolean isOnline() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(getContext()).setTitle("Logout").setMessage("Do you wish to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), "Logout, bye!", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear();
                            editor.apply();
                            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_loginFragment);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CustomArrayAdapter extends ArrayAdapter<Purchase> {

        List<Purchase> purchaseList;

        public CustomArrayAdapter(Context context, int i, List<Purchase> list) {
            super(context, i, list);
            purchaseList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getLayoutInflater().inflate(R.layout.view_row, parent, false);

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String strDate = dateFormat.format(purchaseList.get(position).getDate());

            ((TextView) row.findViewById(R.id.date)).setText(strDate);
            ((TextView) row.findViewById(R.id.total_amount)).setText(purchaseList.get(position).getTotalPrice() + "€");

            return (row);
        }

        public void setProductList(List<Purchase> purchaseList) {
            this.purchaseList = purchaseList;
        }
    }

    private class APIRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.baseUrl + Constants.purchaseUrl + userId);
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

                    Purchase purchaseTemp = new Purchase();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    Date date = format.parse(purchaseJson.get("createdAt").toString());
                    purchaseTemp.setUserId(purchaseJson.get("userId").toString());
                    purchaseTemp.setDate(date);
                    purchaseTemp.setTotalPrice(Double.parseDouble(purchaseJson.get("totalPrice").toString()));

                    if(!purchaseJson.isNull("voucherId")){
                        Voucher voucherTemp = new Voucher();
                        voucherTemp.set_id(purchaseJson.get("voucherId").toString());
                        purchaseTemp.setVoucher(voucherTemp);
                    }

                    List<Product> productsList = new ArrayList<>();
                    JSONArray products = (JSONArray) purchaseJson.get("products");
                    for (int j = 0; j < products.length(); j++) {
                        String _id = products.getJSONObject(j).getString("_id");
                        String name = products.getJSONObject(j).getString("name");
                        String price = products.getJSONObject(j).getString("price");
                        //String url = products.getJSONObject(j).getString("url");
                        Product product = new Product(_id, name, Double.parseDouble(price), "Falta URL");

                        productsList.add(product);
                    }

                    purchaseTemp.setProducts(productsList);
                    purchaseList.add(purchaseTemp);
                }

                //Order list by date
                Collections.sort(purchaseList, new Comparator<Purchase>() {
                    @Override
                    public int compare(Purchase o1, Purchase o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });

                adapter.setProductList(purchaseList);
                list.setAdapter(adapter);


            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
            spinner.setVisibility(View.GONE);
        }
    }

}