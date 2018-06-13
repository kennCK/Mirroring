package com.example.kennck.mirroring;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class ViewPdf extends AppCompatActivity {
    Button back;
    TextView filename;
    WebView viewer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);
        back = (Button) findViewById(R.id.pdfBack);
        filename = (TextView) findViewById(R.id.pdfFilename);
        Intent intent = getIntent();
        filename.setText(intent.getStringExtra("filename"));
        viewer = (WebView) findViewById(R.id.pdfViewer);
        viewer.getSettings().setJavaScriptEnabled(true);
        viewer.loadUrl("https://docs.google.com/viewer?url=" + intent.getStringExtra("url"));
    }

}
