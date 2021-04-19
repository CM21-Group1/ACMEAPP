package org.feup.cm.acmeapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
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

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.PubKey;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.Utils;
import org.feup.cm.acmeapp.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

public class RegisterFragment extends Fragment {
    private String username;
    private String password;
    private String name;
    private String payment_card;
    private View viewTemp;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                    generateAndStoreKeys();
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
//
//        KeyPairGenerator kpg = null;
//        try {
//            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
//            kpg.initialize(new KeyGenParameterSpec.Builder("alias", KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY).setDigests(KeyProperties.DIGEST_SHA256,
//                    KeyProperties.DIGEST_SHA512).build());
//        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        assert kpg != null;
//        KeyPair kp = kpg.generateKeyPair();
//
//        PublicKey publicKey2 = clientKeys.getPublic();
//        PrivateKey privateKey2 = clientKeys.getPrivate();
//
//        System.out.println(publicKey2);
//        System.out.println(privateKey2);

//   -----------------------------------------------------------------------------------------------

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void generateAndStoreKeys() {
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
            if (entry == null) {
                Calendar start = new GregorianCalendar();
                Calendar end = new GregorianCalendar();
                end.add(Calendar.YEAR, 20);
                KeyPairGenerator kgen = KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE);
                AlgorithmParameterSpec spec = new KeyPairGeneratorSpec.Builder(getContext())
                        .setKeySize(Constants.KEY_SIZE)
                        .setAlias(Constants.keyname)
                        .setSubject(new X500Principal("CN=" + Constants.keyname))
                        .setSerialNumber(BigInteger.valueOf(12121212))
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                kgen.initialize(spec);
                KeyPair kp = kgen.generateKeyPair();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    PubKey getPubKey() {
        PubKey pkey = new PubKey();
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
            PublicKey pub = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
            pkey.modulus = ((RSAPublicKey) pub).getModulus().toByteArray();
            pkey.exponent = ((RSAPublicKey) pub).getPublicExponent().toByteArray();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return pkey;
    }

    private class APIRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection = null;
            String publicKey = getPubKey().modulus.toString();

            try {

                jsonBody = new JSONObject();
                jsonBody.put("username", username);
                jsonBody.put("password", password);
                jsonBody.put("name", name);
                jsonBody.put("payment_card", payment_card);
                jsonBody.put("publicKey", publicKey);

                requestBody = Utils.buildPostParameters(jsonBody);
                urlConnection = (HttpURLConnection) Utils.makeRequest("POST", Constants.baseUrl + Constants.registerUrl, null, "application/json", requestBody);
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
                try {
                    JSONObject jsonBody = new JSONObject(response);

                    SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putString(Constants.PREF_USERID, jsonBody.get("id").toString());
                    editor.putString(Constants.PREF_PUBLICKEYSP, jsonBody.get("superPKey").toString());
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Navigation.findNavController(viewTemp).navigate(R.id.action_registerFragment_to_homeFragment);
            }
        }
    }
}