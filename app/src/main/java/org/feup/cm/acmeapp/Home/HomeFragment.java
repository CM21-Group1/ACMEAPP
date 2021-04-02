package org.feup.cm.acmeapp.Home;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.ShoppingCart.ShoppingCartFragment;
import org.feup.cm.acmeapp.register.RegisterViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private BottomNavigationView bottomNavigation;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

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
                    case R.id.unknown:
                        System.out.println("UNKNOWN FRAGMENT");
                        return true;
                    case R.id.shopping_cart:
                        Navigation.findNavController(root).navigate(R.id.action_homeFragment_to_shoppingCartFragment);
                        return true;
                }
                return false;
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

}