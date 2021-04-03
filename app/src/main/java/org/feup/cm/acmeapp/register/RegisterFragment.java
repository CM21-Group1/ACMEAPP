package org.feup.cm.acmeapp.register;

import android.os.AsyncTask;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.Utils;
import org.feup.cm.acmeapp.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RegisterFragment extends Fragment {

    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/auth/register";
    private RegisterViewModel mViewModel;
    private String username;
    private String password;
    private String name;
    private String payment_card;
    private View viewTemp;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    public static KeyPair getKeyPair() {
        KeyPair kp = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            kp = kpg.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return kp;
    }

    //Encrypt the client public key with the embedded dev public key so you can send it home.
    public static String encryptToRSAString(String clearText, String publicKey) {
        String encryptedBase64 = "";
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePublic(keySpec);
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(clearText.getBytes("UTF-8"));
            encryptedBase64 = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedBase64.replaceAll("(\\r|\\n)", "");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        View root = inflater.inflate(R.layout.register_fragment, container, false);

        final Button buttonSignUp = root.findViewById(R.id.btn_register);

        User user = getArguments().getParcelable("user");

        System.out.println(user.getName());

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText username_edittext = root.findViewById(R.id.edit_register_username);
                EditText password_edittext = root.findViewById(R.id.edit_register_pwd);
                EditText name_edittext = root.findViewById(R.id.edit_register_name);
                EditText payment_card_edittext = root.findViewById(R.id.edit_payment);

                username = username_edittext.getText().toString();
                password = password_edittext.getText().toString();
                name = name_edittext.getText().toString();
                payment_card = payment_card_edittext.getText().toString();

                if (username.isEmpty() && password.isEmpty() && name.isEmpty() && payment_card.isEmpty()) {
                    Toast.makeText(getContext(), "All entries are empty", Toast.LENGTH_LONG).show();
                } else if (username.isEmpty()) {
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                } else if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Name empty", Toast.LENGTH_LONG).show();
                } else if (payment_card.isEmpty()) {
                    Toast.makeText(getContext(), "Payment_card empty", Toast.LENGTH_LONG).show();
                } else {
                    viewTemp = view;
                    new APIRequest().execute();
                }
            }
        });

//      --------------------  KEYPAIR TESTES ---------------------------------------------

        KeyPair clientKeys = getKeyPair();
        PublicKey publicKey = clientKeys.getPublic();
        PrivateKey privateKey = clientKeys.getPrivate();

        System.out.println(publicKey);
        System.out.println(privateKey);

        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            kpg.initialize(new KeyGenParameterSpec.Builder("alias", KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY).setDigests(KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512).build());
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        assert kpg != null;
        KeyPair kp = kpg.generateKeyPair();

        PublicKey publicKey2 = clientKeys.getPublic();
        PrivateKey privateKey2 = clientKeys.getPrivate();

        System.out.println(publicKey2);
        System.out.println(privateKey2);

//   -----------------------------------------------------------------------------------------------

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        // TODO: Use the ViewModel
    }


    private class APIRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection = null;
            try {
                jsonBody = new JSONObject();
                jsonBody.put("username", username);
                jsonBody.put("password", password);
                jsonBody.put("name", name);
                jsonBody.put("payment_card", payment_card);
                requestBody = Utils.buildPostParameters(jsonBody);
                urlConnection = (HttpURLConnection) Utils.makeRequest("POST", baseUrl, null, "application/json", requestBody);
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
            } catch (JSONException | IOException e) {
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
            System.out.println(response);
            if (response.equals("{\"message\":\"Username already registered\"}")) {
                Toast.makeText(getContext(), "Username already registered.", Toast.LENGTH_LONG).show();
            } else {
//                try {
//                    JSONObject jsonBody = new JSONObject(response);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Navigation.findNavController(viewTemp).navigate(R.id.action_registerFragment_to_homeFragment);
            }
        }
    }
}