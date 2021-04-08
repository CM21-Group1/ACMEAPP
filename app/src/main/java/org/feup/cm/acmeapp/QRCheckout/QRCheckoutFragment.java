package org.feup.cm.acmeapp.QRCheckout;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
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
import android.widget.ImageView;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QRCheckoutFragment extends Fragment {
    private SharedViewModel sharedViewModel;
    private QRCheckoutViewModel mViewModel;
    private List<Product> productList = new ArrayList<>();
    private Purchase purchase;
    ImageView qrCodeIv;

    public final static int IMAGE_SIZE=900;

    public static QRCheckoutFragment newInstance() {
        return new QRCheckoutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(QRCheckoutViewModel.class);
        View root = inflater.inflate(R.layout.q_r_checkout_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        productList = sharedViewModel.getProductList();
        purchase = sharedViewModel.getPurchase();

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Navigation.findNavController(root).navigate(R.id.action_QRCheckoutFragment_to_homeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        qrCodeIv = root.findViewById(R.id.purchase_qr);

        Button complete = root.findViewById(R.id.complete_btn);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR Code Scan
                Navigation.findNavController(root).navigate(R.id.action_QRCheckoutFragment_to_homeFragment);
            }
        });

        new Thread(new convertToQR(purchase.toString())).start();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(QRCheckoutViewModel.class);
        // TODO: Use the ViewModel
    }

    class convertToQR implements Runnable {
        String content;

        convertToQR(String value) {
            content = value;
        }

        @Override
        public void run() {
            final Bitmap bitmap;

            bitmap = encodeAsBitmap(content);
            getActivity().runOnUiThread(()->qrCodeIv.setImageBitmap(bitmap));
        }
    }

    Bitmap encodeAsBitmap(String str) {
        BitMatrix result;
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "ISO_SET");
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, IMAGE_SIZE, IMAGE_SIZE, null);
        }
        catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int line = 0; line < h; line++) {
            int offset = line * w;
            for (int col = 0; col < w; col++) {
                pixels[offset + col] = result.get(col, line) ? getActivity().getResources().getColor(R.color.black):getActivity().getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}