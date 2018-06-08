package com.example.kennck.mirroring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    Button loginButton;
    Button createAccount;
    EditText username;
    EditText password;
    TextView message;
    String sUsername;
    String sPassword;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button)findViewById(R.id.loginButton);
        createAccount = (Button)findViewById(R.id.createButton);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        message = (TextView) findViewById(R.id.loginMessage);
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, Context.MODE_PRIVATE);

        // Login Account
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sUsername = username.getText().toString();
                sPassword = password.getText().toString();
                if(sUsername.trim().equals("") || sPassword.trim().equals("")){
                    message.setText("Please fill up the required informations.");
                }else{
                    message.setText(null);
                    RequestQueue queue = Volley.newRequestQueue(Login.this);
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            Helper.LOGIN,
                            new Response.Listener<String>(){
                                @Override
                                public void onResponse (String response) {
                                    try {
                                        JSONObject data = new JSONObject(response);
                                        JSONArray dataArray = data.getJSONArray("data");
                                        if(dataArray.length() > 0){
                                            JSONObject object = dataArray.getJSONObject(0);
                                            message.setText(null);
                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putString("username", object.getString("username"));
                                            editor.putString("email", object.getString("email"));
                                            editor.putString("password", sPassword);
                                            editor.putString("id", object.getString("id"));
                                            editor.commit();
                                            // Toast.makeText(Login.this, "Hello " + sUsername + "!", Toast.LENGTH_SHORT).show();
                                            Intent account = new Intent(Login.this, Account.class);
                                            startActivity(account);
                                        }else{
                                            message.setText("Email and Password did not matched!");
                                        }
                                    } catch (JSONException e) {
                                        message.setText("Email and Password did not matched!");
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            }){
                        @Override
                        protected Map<String, String> getParams(){
                            Map<String, String> parameter = new HashMap<String, String>();
                            parameter.put("username", sUsername);
                            parameter.put("password", sPassword);
                            return parameter;
                        }
                    };
                    queue.add(stringRequest);
                }
            }
        });

        // Create Account
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent create = new Intent(Login.this, Registration.class);
                startActivity(create);
            }
        });
    }
}
