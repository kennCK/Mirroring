package com.example.kennck.mirroring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Account extends AppCompatActivity {
    Button logout;
    Button view;
    Button addFile;
    TextView username;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        logout = (Button)findViewById(R.id.accLogout);
        username = (TextView) findViewById(R.id.accUsername);
        view = (Button)findViewById(R.id.viewButton);
        addFile = (Button) findViewById(R.id.addFile);
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, MODE_PRIVATE);
        username.setText(sharedpreferences.getString("username", null));
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
                Intent viewer = new Intent(Account.this, ViewOnSlave.class);
                viewer.putExtra("code", "1234");
                startActivity(viewer);
            }
        });
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add File here
            }
        });
    }

}
