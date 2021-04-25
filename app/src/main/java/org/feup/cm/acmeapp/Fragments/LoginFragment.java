package org.feup.cm.acmeapp.Fragments;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.security.KeyPairGeneratorSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class LoginFragment extends Fragment {

    private String username;
    private String password;
    private View viewTemp;
    private EditText username_edittext;
    private EditText password_edittext;

    private String publicKey;

    private final String DefaultUnameValue = "";
    private String UnameValue;

    private final String DefaultPasswordValue = "";
    private String PasswordValue;

    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.login_fragment, container, false);

        final Button buttonLogin = root.findViewById(R.id.login_btn);
        final Button buttonSignUp = root.findViewById(R.id.register_btn);

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

        username_edittext = root.findViewById(R.id.edit_username);
        password_edittext = root.findViewById(R.id.edit_pwd);
        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
                username_edittext = root.findViewById(R.id.edit_username);
                password_edittext = root.findViewById(R.id.edit_pwd);

                username = username_edittext.getText().toString();
                password = password_edittext.getText().toString();

                if(username.isEmpty() && password.isEmpty()){
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Username & Password empty", Toast.LENGTH_LONG).show();
                }else if(username.isEmpty()){
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                }else if(password.isEmpty()){
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                }else{
                    viewTemp = view;

                    //Creates and stores the keys
                    createAndStoreKey();
                    getPublicKey();

                    new APIRequest().execute();
                }

            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", new User("123", "123"));
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment, bundle);
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

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    private void savePreferences() {
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        UnameValue = username_edittext.getText().toString();
        PasswordValue = password_edittext.getText().toString();

        editor.putString(Constants.PREF_UNAME, UnameValue);
        editor.putString(Constants.PREF_PASSWORD, PasswordValue);
        editor.apply();
    }

    private void loadPreferences() {
        if(username_edittext != null && password_edittext != null){
            SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

            UnameValue = settings.getString(Constants.PREF_UNAME, DefaultUnameValue);
            PasswordValue = settings.getString(Constants.PREF_PASSWORD, DefaultPasswordValue);
            username_edittext.setText(UnameValue);
            password_edittext.setText(PasswordValue);
        }
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
                jsonBody.put("publicKey",publicKey);

                requestBody = Utils.buildPostParameters(jsonBody);
                urlConnection = (HttpURLConnection) Utils.makeRequest("POST", Constants.baseUrl + Constants.loginUrl, null, "application/json", requestBody);
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
            if(response.equals("{\"message\":\"Username Not found.\"}")){
                spinner.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Username Not found.", Toast.LENGTH_LONG).show();
            }else if(response.equals("{\"message\":\"Password incorrect\"}")){
                spinner.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Password incorrect.", Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONObject jsonBody = new JSONObject(response);
                    System.out.println(jsonBody);
                    SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

                    //Saves the new user information
                    savePreferences();

                    //Also saves the id and supermarket public key
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.PREF_USERID, jsonBody.get("id").toString());
                    editor.putString(Constants.PREF_PUBLICKEYSP, jsonBody.get("superPKey").toString());
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Navigation.findNavController(viewTemp).navigate(R.id.action_loginFragment_to_homeFragment);
            }
        }
    }


    //Creates and stores the user private and public key and stores them in the keystore igual no login, fazer refactor
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
            publicKey = new KeyPart(((RSAPublicKey)pub).getModulus().toByteArray(),((RSAPublicKey)pub).getPublicExponent().toByteArray()).toString();
        }catch (Exception e){
            System.out.println(e + " in load of public the key");
        }
    }

}

