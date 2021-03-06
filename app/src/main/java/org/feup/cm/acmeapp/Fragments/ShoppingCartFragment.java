package org.feup.cm.acmeapp.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.ProductDecrypter;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.app.Activity.RESULT_OK;

public class ShoppingCartFragment extends Fragment{
    private SharedViewModel sharedViewModel;
    private BottomNavigationView bottomNavigation;
    private ListView list;
    private List<Product> productList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private String supermakerPublicKey;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.shopping_cart_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        bottomNavigation = root.findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.shopping_cart);

        setHasOptionsMenu(true);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        if(!productList.isEmpty()){
                            sharedViewModel.setProductList(productList);
                        }
                        Navigation.findNavController(root).navigate(R.id.action_shoppingCartFragment_to_homeFragment);
                        return true;
                    case R.id.vouchers:
                        if(!productList.isEmpty()){
                            sharedViewModel.setProductList(productList);
                        }
                        Navigation.findNavController(root).navigate(R.id.action_shoppingCartFragment_to_vouchersFragment);
                        return true;
                    case R.id.shopping_cart:
                        System.out.println("SHOPPING CART FRAGMENT");
                        return true;
                }
                return false;
            }
        });

        list = root.findViewById(R.id.array_listview);
        adapter = new CustomArrayAdapter(getContext(), 0, productList);

        if(sharedViewModel.getProductList() != null){
            productList = sharedViewModel.getProductList();
            updateProductList();
        }

        FloatingActionButton addProduct = root.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR Code Scan
                scan(true);
            }
        });

        FloatingActionButton proceedCheckout = root.findViewById(R.id.proceedCheckout);
        proceedCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Proceed to Checkout
                if(productList.isEmpty()){
                    Toast.makeText(getContext(), "No products added to the cart", Toast.LENGTH_LONG).show();
                }else{
                    sharedViewModel.setProductList(productList);
                    Navigation.findNavController(root).navigate(R.id.action_shoppingCartFragment_to_checkoutFragment);
                }
            }
        });

        //If list is empty
        list.setEmptyView(root.findViewById(R.id.empty_list));

        //Get the public key from supermarket
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        supermakerPublicKey = settings.getString(Constants.PREF_PUBLICKEYSP, "");

        //teste (remove)
        //productList.add(new Product("Teste", "test", Math.random(), "teste"));

        updateProductList();

        return root;
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

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //bundle.putCharSequence("Message", message); erro
    }

    public void scan(boolean qrcode) {
        try {
            Intent intent = new Intent(Constants.ACTION_SCAN);
            intent.putExtra("SCAN_MODE", qrcode ? "QR_CODE_MODE" : "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            showDialog(getActivity(), "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                act.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(buttonNo, null);
        return downloadDialog.show();
    }

    //Called after the Qr scan
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //Gets the contents of the qr
                String contents = data.getStringExtra("SCAN_RESULT");

                System.out.println("Conteudo QR:" +contents);

                //Bool error reading QR
                Boolean error = false;

                //Creates the ProductDecrypter
                ProductDecrypter productDecrypter = new ProductDecrypter(supermakerPublicKey, contents);;

                if (error){
                    System.out.println("QR Invalido");
                }
                if (productDecrypter.getProduct()!= null){
                    //Aux variable if the product exists
                    Boolean productExists = false;
                    for (Product pr : productList){
                        //if the product already exists increases the quantity
                        System.out.println(pr);
                        if (pr.getProductCode().equals(productDecrypter.getProduct().getProductCode())){
                            pr.increaseQuantity();
                            productExists = true;
                        }
                    }

                    if (!productExists){
                        //Adds the product to the product list
                        productList.add(productDecrypter.getProduct());
                    }

                    updateProductList();
                }

            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateProductList() {
        adapter = new CustomArrayAdapter(getContext(), 0, productList);
        adapter.setProductList(productList);
        list.setAdapter(adapter);
    }

    private class CustomArrayAdapter extends ArrayAdapter<Product> {

        List<Product> productList;

        public CustomArrayAdapter(Context context, int i, List<Product> list) {
            super(context, i, list);
            productList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getLayoutInflater().inflate(R.layout.row, parent, false);

            ((TextView) row.findViewById(R.id.product_name)).setText(productList.get(position).getName());
            ((TextView) row.findViewById(R.id.product_price)).setText(productList.get(position).getPriceStr() + "???");
            ((TextView) row.findViewById(R.id.product_quantity)).setText(String.valueOf(productList.get(position).getQuantity()) );
            new DownloadImageTask((ImageView) row.findViewById(R.id.product_image)).execute(productList.get(position).getImageUrl());

            ImageButton deleteBtn = row.findViewById(R.id.delete_product);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productList.remove(position);
                    updateProductList();
                }
            });

            return (row);
        }

        public void setProductList(List<Product> productList) {
            this.productList = productList;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}