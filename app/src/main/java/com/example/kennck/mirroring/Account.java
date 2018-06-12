package com.example.kennck.mirroring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Account extends AppCompatActivity {
    Button logout;
    Button view;
    TextView username;
    SharedPreferences sharedpreferences;
    FloatingActionButton add;
    ArrayAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        logout = (Button)findViewById(R.id.accLogout);
        username = (TextView) findViewById(R.id.accUsername);
        view = (Button)findViewById(R.id.viewButton);
        listView = (ListView) findViewById(R.id.recordList);
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, MODE_PRIVATE);
        username.setText(sharedpreferences.getString("username", null));
        add = (FloatingActionButton) findViewById(R.id.addFile);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                sharedpreferences.edit().clear().commit();
                Intent login = new Intent(Account.this, Login.class);
                startActivity(login);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkCode = new Intent(Account.this, CheckCode.class);
                startActivity(checkCode);
            }
        });
        retrieveRecords();
    }

    public void retrieveRecords(){
        RequestQueue queue = Volley.newRequestQueue(Account.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Helper.RETRIEVE_RECORD,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse (String response) {
                        try {
                            JSONObject data = new JSONObject(response);
                            JSONArray dataArray = data.getJSONArray("data");
                            if(dataArray.length() > 0){
                                displayRecords(response);
                            }else{
                                displayRecordsEmpty("Empty");
                            }
                        } catch (JSONException e) {
                            displayRecordsEmpty("Empty");
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
                parameter.put("value", sharedpreferences.getString("id", null));
                parameter.put("column", "id");
                return parameter;
            }
        };
        queue.add(stringRequest);
    }

    public void displayRecords(String response) throws JSONException {
        JSONObject data = new JSONObject(response);
        JSONArray dataArray = data.getJSONArray("data");
        String[] mobileArray = new String[dataArray.length()];
        for (int i = 0; i < dataArray.length(); i++){
            JSONObject object = dataArray.getJSONObject(i);
            mobileArray[i] = object.getString("code");
        }
        adapter = new ArrayAdapter<String>(this, R.layout.content_list_view, mobileArray);
        listView.setAdapter(adapter);
    }

    public void  displayRecordsEmpty(String str){
        String[] array = {str};
        adapter = new ArrayAdapter<String>(this, R.layout.content_list_view, array);
        listView.setAdapter(adapter);
    }


}
