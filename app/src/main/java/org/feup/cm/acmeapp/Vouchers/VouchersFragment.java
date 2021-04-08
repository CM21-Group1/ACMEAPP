package org.feup.cm.acmeapp.Vouchers;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.feup.cm.acmeapp.R;
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

    private VouchersViewModel mViewModel;
    private BottomNavigationView bottomNavigation;

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_USERID ="User ID";

    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/sp/vouchers/";

    private ListView list;
    private List<Voucher> voucherList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private String userId;

    public static VouchersFragment newInstance() {
        return new VouchersFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(VouchersViewModel.class);
        View root = inflater.inflate(R.layout.vouchers_fragment, container, false);

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
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        userId = settings.getString(PREF_USERID, "");
        //System.out.println(userId);

        list = root.findViewById(R.id.vouchers_listview);
        adapter = new CustomArrayAdapter(getContext(), 0, voucherList);

        // Get all purchases from customer from user id
        new APIRequest().execute();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(VouchersViewModel.class);
        // TODO: Use the ViewModel
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

    private class APIRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(baseUrl + userId);
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

                adapter.setVouchersList(voucherList);
                list.setAdapter(adapter);


            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }
    }
}