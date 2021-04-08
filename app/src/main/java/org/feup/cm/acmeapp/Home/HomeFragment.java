package org.feup.cm.acmeapp.Home;

import androidx.fragment.app.FragmentManager;
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
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.feup.cm.acmeapp.CustomDialog;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
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

    private HomeViewModel mViewModel;
    private BottomNavigationView bottomNavigation;

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_USERID ="User ID";

    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/sp/purchase/";

    private ListView list;
    private List<Purchase> purchaseList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private String userId;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.home_fragment, container, false);

        bottomNavigation = root.findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.home);

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
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        userId = settings.getString(PREF_USERID, "");
        //System.out.println(userId);

        list = root.findViewById(R.id.purchases_listview);
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

                CustomDialog cdd = new CustomDialog(getActivity(), purchaseClicked.getProducts());
                cdd.show();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
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
            ((TextView) row.findViewById(R.id.total_amount)).setText(String.valueOf(purchaseList.get(position).getTotalPrice()) + "€");

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

                    Purchase purchaseTemp = new Purchase();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    Date date = format.parse(purchaseJson.get("createdAt").toString());
                    purchaseTemp.setUserId(purchaseJson.get("userId").toString());
                    purchaseTemp.setDate(date);
                    purchaseTemp.setTotalPrice(Double.parseDouble(purchaseJson.get("totalPrice").toString()));

                    List<Product> productsList = new ArrayList<>();
                    JSONArray products = (JSONArray) purchaseJson.get("products");
                    for (int j = 0; j < products.length(); j++)
                    {
                        String _id = products.getJSONObject(j).getString("_id");
                        String name = products.getJSONObject(j).getString("name");
                        String price = products.getJSONObject(j).getString("price");
                        Product product = new Product(_id, name, Double.parseDouble(price));

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

        }
    }
}