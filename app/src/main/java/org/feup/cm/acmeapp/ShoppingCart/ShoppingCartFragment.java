package org.feup.cm.acmeapp.ShoppingCart;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class ShoppingCartFragment extends Fragment {

    private ShoppingCartViewModel mViewModel;

    private BottomNavigationView bottomNavigation;
    private ListView list;

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
                //QR Code Scan
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