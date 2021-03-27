package org.feup.cm.acmeapp.login;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private String username;
    private String password;
    private View viewTemp;
    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/auth/login";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.login_fragment, container, false);

        final Button buttonLogin = root.findViewById(R.id.login_btn);
        final Button buttonSignUp = root.findViewById(R.id.register_btn);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText username_edittext = root.findViewById(R.id.edit_username);
                EditText password_edittext = root.findViewById(R.id.edit_pwd);

                username = username_edittext.getText().toString();
                password = password_edittext.getText().toString();


                if(username.isEmpty() && password.isEmpty()){
                    Toast.makeText(getContext(), "Username & Password empty", Toast.LENGTH_LONG).show();
                }else if(username.isEmpty()){
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                }else{
                    viewTemp = view;
                    new APIRequest().execute();
                }

            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
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
            if(response.equals("{\"message\":\"Username Not found.\"}")){
                Toast.makeText(getContext(), "Username Not found.", Toast.LENGTH_LONG).show();
            }else if(response.equals("{\"message\":\"Password incorrect\"}")){
                Toast.makeText(getContext(), "Password incorrect.", Toast.LENGTH_LONG).show();
            }else{
//                try {
//                    JSONObject jsonBody = new JSONObject(response);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Navigation.findNavController(viewTemp).navigate(R.id.action_loginFragment_to_homeFragment);
            }
        }
    }
}

