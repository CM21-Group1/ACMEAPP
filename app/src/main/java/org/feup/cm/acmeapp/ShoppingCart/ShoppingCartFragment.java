package org.feup.cm.acmeapp.ShoppingCart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.ProductDecrypter;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ShoppingCartFragment extends Fragment{
    private SharedViewModel sharedViewModel;

    private ShoppingCartViewModel mViewModel;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private BottomNavigationView bottomNavigation;
    private ListView list;
    private List<Product> productList = new ArrayList<>();
    private CustomArrayAdapter adapter;

    public static ShoppingCartFragment newInstance() {
        return new ShoppingCartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        View root = inflater.inflate(R.layout.shopping_cart_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        bottomNavigation = root.findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.shopping_cart);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Navigation.findNavController(root).navigate(R.id.action_shoppingCartFragment_to_homeFragment);
                        return true;
                    case R.id.vouchers:
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
                //TEST
//                    Product productTemp = new Product("teste", "Product x", 10);
//                    productList.add(productTemp);
//                    updateProductList();
                //TEST
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

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //bundle.putCharSequence("Message", message); erro
    }

    public void onRestoreInstanceState(Bundle bundle) {
        //super.onRestoreInstanceState(bundle);
        //message = bundle.getCharSequence("Message").toString(); erro
    }

    public void scan(boolean qrcode) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", qrcode ? "QR_CODE_MODE" : "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            //showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");

                System.out.println("Format: " + format + "\nMessage: " + contents);

                ProductDecrypter productDecrypter = new ProductDecrypter("falta", contents);
                productList.add(productDecrypter.getProduct());
                updateProductList();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
    }

    private void updateProductList() {
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
            ((TextView) row.findViewById(R.id.product_price)).setText(String.valueOf(productList.get(position).getPrice()) + "€");

            return (row);
        }

        public void setProductList(List<Product> productList) {
            this.productList = productList;
        }
    }
}