package com.example.kennck.mirroring;

import android.content.Intent;
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

public class Registration extends AppCompatActivity {
    Button loginButton;
    Button registerButton;
    EditText newUsername;
    EditText newEmail;
    EditText newPassword;
    EditText cNewPassword;
    TextView message;
    String sNewUsername;
    String sNewEmail;
    String sNewPassword;
    String sCNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        registerButton = (Button)findViewById(R.id.registerButton);
        loginButton = (Button)findViewById(R.id.backToLogin);
        newUsername = (EditText) findViewById(R.id.newUsername);
        newEmail = (EditText) findViewById(R.id.newEmailAddress);
        newPassword = (EditText) findViewById(R.id.newPassword);
        cNewPassword = (EditText) findViewById(R.id.confirmNewPassword);
        message = (TextView) findViewById(R.id.registrationMessage);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sNewUsername = newUsername.getText().toString();
                sNewEmail = newEmail.getText().toString();
                sNewPassword = newPassword.getText().toString();
                sCNewPassword = cNewPassword.getText().toString();

                if(sNewUsername.trim().equals("") || sNewEmail.trim().equals("") || sNewPassword.trim().equals("")){
                    message.setText("Please fill up the required informations");
                }else if(sNewUsername.length() < 6){
                    message.setText("Username must not be less than to 6 Characters");
                }else if(!sNewPassword.equals(sCNewPassword)){
                    message.setText("Please confirm your password");
                }else{
                    RequestQueue queue = Volley.newRequestQueue(Registration.this);
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            Helper.CREATE_ACCOUNT,
                            new Response.Listener<String>(){
                                @Override
                                public void onResponse (String response) {
                                    try {
                                        JSONObject data = new JSONObject(response);
                                        if(data.getInt("data") > 0){
                                            message.setText(null);
                                            Toast.makeText(Registration.this, "Account Successfully Created!", Toast.LENGTH_LONG).show();
                                        }else{
                                            message.setText("Unable to Create an Account!");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
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
                            parameter.put("username", sNewUsername);
                            parameter.put("email", sNewEmail);
                            parameter.put("password", sNewPassword);
                            return parameter;
                        }
                    };
                    queue.add(stringRequest);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back to Login
                Intent backTologin = new Intent(Registration.this, Login.class);
                startActivity(backTologin);
            }
        });
    }
}
