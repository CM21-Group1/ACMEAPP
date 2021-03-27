package org.feup.cm.acmeapp.register;

import androidx.lifecycle.ViewModelProvider;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.Utils;
import org.feup.cm.acmeapp.login.LoginFragment;
import org.feup.cm.acmeapp.login.LoginViewModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class RegisterFragment extends Fragment {

    private RegisterViewModel mViewModel;
    private String username;
    private String password;
    private String name;
    private String payment_card;
    private View viewTemp;
    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/auth/register";

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        View root = inflater.inflate(R.layout.register_fragment, container, false);

        final Button buttonSignUp = root.findViewById(R.id.btn_register);

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

                if(username.isEmpty() && password.isEmpty() && name.isEmpty() && payment_card.isEmpty()){
                    Toast.makeText(getContext(), "All entries are empty", Toast.LENGTH_LONG).show();
                }else if(username.isEmpty()){
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                }else if(name.isEmpty()){
                    Toast.makeText(getContext(), "Name empty", Toast.LENGTH_LONG).show();
                }else if(payment_card.isEmpty()){
                    Toast.makeText(getContext(), "Payment_card empty", Toast.LENGTH_LONG).show();
                }else{
                    viewTemp = view;
                    new APIRequest().execute();
                }
            }
        });

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
            if(response.equals("{\"message\":\"Username already registered\"}")){
                Toast.makeText(getContext(), "Username already registered.", Toast.LENGTH_LONG).show();
            }else{
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