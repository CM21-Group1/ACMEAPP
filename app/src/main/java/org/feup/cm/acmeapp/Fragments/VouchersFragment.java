package org.feup.cm.acmeapp.Fragments;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
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

public class VouchersFragment extends Fragment {

    private BottomNavigationView bottomNavigation;
    private ListView list;
    private List<Voucher> voucherList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private String userId;
    private SharedViewModel sharedViewModel;

    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.vouchers_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

//        TODO
//         Check internet connection dialog. Only dismiss if the internet connection back online again
//         Do this dialog in every fragment to check connection
//         ######################################################################################################################
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
                                    new APIRequestGetVouchers().execute();
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
        // TODO
        //  #########################################################################################################################

        setHasOptionsMenu(true);
        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        bottomNavigation = root.findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.vouchers);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Navigation.findNavController(root).navigate(R.id.action_vouchersFragment_to_homeFragment);
                        return true;
                    case R.id.vouchers:
                        System.out.println("VOUCHER FRAGMENT");
                        return true;
                    case R.id.shopping_cart:
                        Navigation.findNavController(root).navigate(R.id.action_vouchersFragment_to_shoppingCartFragment);
                        return true;
                }
                return false;
            }
        });

        // USER ID RETRIEVED FROM SHAREDPREFERENCES
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        userId = settings.getString(Constants.PREF_USERID, "");
        //System.out.println(userId);

        list = root.findViewById(R.id.vouchers_listview);
        list.setEmptyView(root.findViewById(R.id.empty_list));
        adapter = new CustomArrayAdapter(getContext(), 0, voucherList);

        // Get all vouchers from customer from user id
        new APIRequestGetVouchers().execute();

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
                            Navigation.findNavController(getView()).navigate(R.id.action_shoppingCartFragment_to_loginFragment);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class CustomArrayAdapter extends ArrayAdapter<Voucher> {

        List<Voucher> vouchersList;

        public CustomArrayAdapter(Context context, int i, List<Voucher> list) {
            super(context, i, list);
            vouchersList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getLayoutInflater().inflate(R.layout.voucher_row, parent, false);

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String strDate = dateFormat.format(vouchersList.get(position).getCreatedAt());

            ((TextView) row.findViewById(R.id.date)).setText(strDate);

            return (row);
        }

        public void setVouchersList(List<Voucher> vouchersList) {
            this.vouchersList = vouchersList;
        }
    }

    private class APIRequestGetVouchers extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.baseUrl + Constants.vouchersUrl + userId);
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

                    voucherList.add(voucherTemp);

                }

                //Order list by date
                Collections.sort(voucherList, new Comparator<Voucher>() {
                    @Override
                    public int compare(Voucher o1, Voucher o2) {
                        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                    }
                });

                sharedViewModel.setVoucherList(voucherList);
                adapter.setVouchersList(voucherList);
                list.setAdapter(adapter);
                spinner.setVisibility(View.GONE);

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }
    }
}