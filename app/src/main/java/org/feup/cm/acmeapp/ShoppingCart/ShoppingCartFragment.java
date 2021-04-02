package org.feup.cm.acmeapp.ShoppingCart;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.User;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ShoppingCartFragment extends Fragment {

    private ShoppingCartViewModel mViewModel;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private BottomNavigationView bottomNavigation;
    private ListView list;
    String message;


    public static ShoppingCartFragment newInstance() {
        return new ShoppingCartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        View root = inflater.inflate(R.layout.shopping_cart_fragment, container, false);

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
                    case R.id.unknown:
                        System.out.println("UNKNOWN FRAGMENT");
                        return true;
                    case R.id.shopping_cart:
                        System.out.println("SHOPPING CART FRAGMENT");
                        return true;
                }
                return false;
            }
        });

        list = root.findViewById(R.id.array_listview);

        FloatingActionButton addProduct = root.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan(true);
            }
        });

        FloatingActionButton proceedCheckout = root.findViewById(R.id.proceedCheckout);
        proceedCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Proceed to Checkout
            }
        });

        //If list is empty
        list.setEmptyView(root.findViewById(R.id.empty_list));

        //Populate list
        setProductList();

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putCharSequence("Message", message);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        //super.onRestoreInstanceState(bundle);
        message = bundle.getCharSequence("Message").toString();
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

                message = "Format: " + format + "\nMessage: " + contents;
                System.out.println(message);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setProductList(){
        List<Product> productList= new ArrayList<>();

        Product i= new Product("foo", "10");
        Product i2= new Product("fooeqw", "20");
        Product i3= new Product("foeqweo", "30");

        productList.add(i);
        productList.add(i2);
        productList.add(i3);

        list.setAdapter(new CustomArrayAdapter(getContext(), 0, productList));
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
            ((TextView) row.findViewById(R.id.product_price)).setText(productList.get(position).getPrice());

            return (row);
        }
    }
}