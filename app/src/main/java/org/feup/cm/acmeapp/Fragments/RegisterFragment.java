package org.feup.cm.acmeapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import java.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.Security.KeyPart;
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
    private String publicKey;
    private View viewTemp;
    private ProgressBar spinner;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.register_fragment, container, false);

        final Button buttonSignUp = root.findViewById(R.id.btn_register);
        final Button buttonBack = root.findViewById(R.id.btn_back);

        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

//        TODO
//         Check internet connection dialog. Only dismiss if the internet connection back online again
//         Do this dialog in every fragment to check connection
//         ######################################################################################################################
        if(!isOnline()){
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Info");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage("Internet not available, Cross check your internet connectivity and try again");
                builder.setCancelable(false);
                builder.setPositiveButton("Retry", null);

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                if(isOnline()){
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });

                dialog.show();
            } catch (Exception e) {
                System.out.println();
            }
        }
        // TODO
        //  #########################################################################################################################

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
                EditText username_edittext = root.findViewById(R.id.edit_register_username);
                EditText password_edittext = root.findViewById(R.id.edit_register_pwd);
                EditText name_edittext = root.findViewById(R.id.edit_register_name);
                EditText payment_card_edittext = root.findViewById(R.id.edit_payment);

                username = username_edittext.getText().toString();
                password = password_edittext.getText().toString();
                name = name_edittext.getText().toString();
                payment_card = payment_card_edittext.getText().toString();

                if (username.isEmpty() && password.isEmpty() && name.isEmpty() && payment_card.isEmpty()) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "All entries are empty", Toast.LENGTH_LONG).show();
                } else if (username.isEmpty()) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                } else if (password.isEmpty()) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                } else if (name.isEmpty()) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Name empty", Toast.LENGTH_LONG).show();
                } else if (payment_card.isEmpty()) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Payment_card empty", Toast.LENGTH_LONG).show();
                } else {
                    viewTemp = view;

                    //Creates and stores the keys
                    createAndStoreKey();
                    getPublicKey();

                    //Makes the request
                    new APIRequestCreateUser().execute();
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).popBackStack();
            }
        });

        return root;
    }

    public boolean isOnline() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    //Saves the user name and password in the user preferences
    private void savePreferences() {
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(Constants.PREF_UNAME, username);
        editor.putString(Constants.PREF_PASSWORD, password);
        editor.apply();
    }

    //Creates and stores the user private and public key and stores them in the keystore
    private void createAndStoreKey(){
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);

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

        }catch (Exception e){
            System.out.println(e + " in creation of the key");
        }
    }

    //Stores the created key
    private void getPublicKey() {
        try {
            //Gets the KeyStore
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);

            //Creates the two adapter class keys
            PublicKey pub = ((KeyStore.PrivateKeyEntry)entry).getCertificate().getPublicKey();
            publicKey = Base64.getEncoder().encodeToString(pub.getEncoded());//new KeyPart(((RSAPublicKey)pub).getModulus().toByteArray(),((RSAPublicKey)pub).getPublicExponent().toByteArray()).toString();new KeyPart(((RSAPublicKey)pub).getModulus().toByteArray(),((RSAPublicKey)pub).getPublicExponent().toByteArray()).toString();
        }catch (Exception e){
            System.out.println(e + " in load of public the key");
        }
    }

    private class APIRequestCreateUser extends AsyncTask<Void, Void, String> {

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
                jsonBody.put("publicKey",publicKey);

                System.out.println(jsonBody);

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
            if (response.equals("{\"message\":\"Username already registered\"}")) {
                spinner.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Username already registered.", Toast.LENGTH_LONG).show();
            } else {
                try {
                    //Stores User information
                    savePreferences();

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