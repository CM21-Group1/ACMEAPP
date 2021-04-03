package org.feup.cm.acmeapp.login;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private String username;
    private String password;
    private View viewTemp;
    private EditText username_edittext;
    private EditText password_edittext;
    private final String baseUrl = "https://acmeapi-cm.herokuapp.com/auth/login";

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";
    private static final String PREF_USERID ="User ID";

    private final String DefaultUnameValue = "";
    private String UnameValue;

    private final String DefaultPasswordValue = "";
    private String PasswordValue;

    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.login_fragment, container, false);

        final Button buttonLogin = root.findViewById(R.id.login_btn);
        final Button buttonSignUp = root.findViewById(R.id.register_btn);

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
                    Toast.makeText(getContext(), "Username & Password empty", Toast.LENGTH_LONG).show();
                }else if(username.isEmpty()){
                    Toast.makeText(getContext(), "Username empty", Toast.LENGTH_LONG).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getContext(), "Password empty", Toast.LENGTH_LONG).show();
                }else{
                    viewTemp = view;
                    savePreferences();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        // TODO: Use the ViewModel
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
        SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        UnameValue = username_edittext.getText().toString();
        PasswordValue = password_edittext.getText().toString();

        editor.putString(PREF_UNAME, UnameValue);
        editor.putString(PREF_PASSWORD, PasswordValue);
        editor.apply();
    }

    private void loadPreferences() {
        if(username_edittext != null && password_edittext != null){
            SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            UnameValue = settings.getString(PREF_UNAME, DefaultUnameValue);
            PasswordValue = settings.getString(PREF_PASSWORD, DefaultPasswordValue);
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
                try {
                    JSONObject jsonBody = new JSONObject(response);

                    SharedPreferences settings = getActivity().getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putString(PREF_USERID, jsonBody.get("id").toString());
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Navigation.findNavController(viewTemp).navigate(R.id.action_loginFragment_to_homeFragment);
            }
        }
    }
}

