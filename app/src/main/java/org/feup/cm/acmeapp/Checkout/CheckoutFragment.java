package org.feup.cm.acmeapp.Checkout;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.w3c.dom.Text;

import java.util.List;

public class CheckoutFragment extends Fragment{
    private SharedViewModel sharedViewModel;
    private CheckoutViewModel mViewModel;
    private List<Product> productList;

    private double totalAmount = 0;

    public static CheckoutFragment newInstance() {
        return new CheckoutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        View root = inflater.inflate(R.layout.checkout_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        productList = sharedViewModel.getProductList();

        for (Product product: productList) {
            totalAmount += product.getPrice();
        }

        TextView txt = root.findViewById(R.id.subTotalPrice);
        txt.setText(totalAmount + "€");

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Navigation.findNavController(root).navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        Button pay_btn = root.findViewById(R.id.pay_btn);
        pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR Code Scan
                Navigation.findNavController(root).navigate(R.id.action_checkoutFragment_to_QRCheckoutFragment);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        // TODO: Use the ViewModel
    }
}