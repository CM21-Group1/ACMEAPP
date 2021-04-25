package org.feup.cm.acmeapp.Fragments;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QRCheckoutFragment extends Fragment {
    private SharedViewModel sharedViewModel;
    private Purchase purchase;
    ImageView qrCodeIv;
    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.q_r_checkout_fragment, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        sharedViewModel.setProductList(null);
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


        new Thread(new convertToQR(purchase.QRCodeString())).start();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    class convertToQR implements Runnable {
        String content;

        convertToQR(String mess) {
            content = mess;
        }

        @Override
        public void run() {
            final Bitmap bitmap;

            bitmap = encodeAsBitmap(content);
            getActivity().runOnUiThread(()->qrCodeIv.setImageBitmap(bitmap));
            spinner.setVisibility(View.GONE);
        }
    }

    Bitmap encodeAsBitmap(String str) {
        BitMatrix result;
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "ISO_SET");
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, Constants.IMAGE_SIZE, Constants.IMAGE_SIZE, null);
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