package org.feup.cm.acmeapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.Utils;
import org.feup.cm.acmeapp.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

public class RegisterFragment extends Fragment {
    private String username;
    private String password;
    private String name;
    private String payment_card;
    private View viewTemp;
    private SharedViewModel sharedViewModel;

    private ProgressBar spinner;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.register_fragment, container, false);

        final Button buttonSignUp = root.findViewById(R.id.btn_register);
        final Button buttonBack = root.findViewById(R.id.btn_back);
        spinner = root.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        User user = getArguments().getParcelable("user");

        System.out.println(user.getName());

        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

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


                    try {
                        KeyPairGenerator keyGen = null;
                        keyGen = KeyPairGenerator.getInstance(Constants.KEY_ALGO);
                        keyGen.initialize(Constants.KEY_SIZE);
                        KeyPair pair = keyGen.generateKeyPair();

                        sharedViewModel.setPersonalPrivateKey(pair.getPrivate());
                        sharedViewModel.setPersonalPublicKey(pair.getPublic());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    savePreferences();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void savePreferences() {
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(Constants.PREF_UNAME, username);
        editor.putString(Constants.PREF_PASSWORD, password);
        editor.apply();
    }


    private class APIRequestCreateUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            JSONObject jsonBody;
            String requestBody;
            HttpURLConnection urlConnection = null;
            String publicKey = ((RSAPublicKey) sharedViewModel.getPersonalPublicKey()).getModulus().toString();

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
            if (response.equals("{\"message\":\"Username already registered\"}")) {
                spinner.setVisibility(View.GONE);
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