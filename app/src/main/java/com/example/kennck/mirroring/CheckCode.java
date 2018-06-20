package com.example.kennck.mirroring;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class CheckCode extends AppCompatActivity {
    Button back;
    Button check;
    TextView message;
    EditText code;
    String sCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_code);

        back = (Button) findViewById(R.id.ccBack);
        check = (Button) findViewById(R.id.ccCheck);
        code = (EditText) findViewById(R.id.ccCode);
        message = (TextView) findViewById(R.id.ccErrorMessage);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckCode.this, Account.class);
                startActivity(intent);
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sCode = code.getText().toString();
                if(sCode.trim().equals("")){
                    message.setText("Code is Empty!");
                }else{
                    RequestQueue queue = Volley.newRequestQueue(CheckCode.this);
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            Helper.RETRIEVE_RECORD_CUSTOM,
                            new Response.Listener<String>(){
                                @Override
                                public void onResponse (String response) {
                                    try {
                                        message.setText(null);
                                        JSONObject data = new JSONObject(response);
                                        JSONArray dataArray = data.getJSONArray("data");
                                        if(dataArray.length() > 0){
                                            JSONObject object = dataArray.getJSONObject(0);
                                            Intent intent = new Intent(CheckCode.this, ViewOnSlave.class);
                                            intent.putExtra("code", object.getString("code"));
                                            intent.putExtra("url", object.getString("url"));
                                            startActivity(intent);
                                        }else{
                                            message.setText("Code not found!");
                                        }
                                    } catch (JSONException e) {
                                        message.setText("Code not found!");
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    message.setText("Code not found!");
                                }
                            }){
                        @Override
                        protected Map<String, String> getParams(){
                            Map<String, String> parameter = new HashMap<String, String>();
                            parameter.put("value", sCode);
                            parameter.put("column", "code");
                            return parameter;
                        }
                    };
                    queue.add(stringRequest);
                }
            }
        });

    }

}
