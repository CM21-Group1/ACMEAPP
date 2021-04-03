package org.feup.cm.acmeapp.ShoppingCart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartFragment extends Fragment {

    private ShoppingCartViewModel mViewModel;

    private BottomNavigationView bottomNavigation;
    private ListView list;
    private List<Product> productList = new ArrayList<>();
    private CustomArrayAdapter adapter;
    private int listRowPosition;

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
        adapter = new CustomArrayAdapter(getContext(), 0, productList);

        FloatingActionButton addProduct = root.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR Code Scan
                setProductList();
            }
        });

        FloatingActionButton proceedCheckout = root.findViewById(R.id.proceedCheckout);
        proceedCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Proceed to Checkout
                Navigation.findNavController(root).navigate(R.id.action_shoppingCartFragment_to_checkoutFragment);
            }
        });

//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                listRowPosition = position;
//                Button del = (Button) view.findViewById(R.id.deleteProduct);
//                del.setOnClickListener(
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                int pos = v.getId();
//                                System.out.println(pos);
//                                Product toRemove = adapter.getItem(pos);
//                                adapter.remove(toRemove);
//                            }
//                        });
//            }
//        });

        //If list is empty
        list.setEmptyView(root.findViewById(R.id.empty_list));

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ShoppingCartViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setProductList() {
        Product i = new Product("foo", "10");
        productList.add(i);
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
            ((TextView) row.findViewById(R.id.product_price)).setText(productList.get(position).getPrice());

            return (row);
        }

        public void setProductList(List<Product> productList) {
            this.productList = productList;
        }
    }
}