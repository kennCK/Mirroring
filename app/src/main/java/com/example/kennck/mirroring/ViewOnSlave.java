package com.example.kennck.mirroring;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ViewOnSlave extends AppCompatActivity {
    Button back;
    TextView code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_slave);
        back = (Button) findViewById(R.id.vsBack);
        code = (TextView) findViewById(R.id.vsCode);
        Intent intent = getIntent();
        code.setText(intent.getStringExtra("code"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent account = new Intent(ViewOnSlave.this, Account.class);
                startActivity(account);
            }
        });

    }

}
