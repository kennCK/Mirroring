package com.example.kennck.mirroring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class ViewRecorded extends AppCompatActivity {
    Button back;
    TextView filename;
    VideoView videoView;
    ProgressDialog progressDialog;
    MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recorded);
        back = (Button) findViewById(R.id.vrBack);
        filename = (TextView) findViewById(R.id.vrFilename);
        Intent intent = getIntent();
        filename.setText(intent.getStringExtra("filename"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent account = new Intent(ViewRecorded.this, Account.class);
                startActivity(account);
            }
        });
        videoView = (VideoView) findViewById(R.id.videoViewRecorded);
        progressDialog = new ProgressDialog(ViewRecorded.this);
        progressDialog.setTitle("Screen Mirroring");
        progressDialog.setMessage("Bufferring....");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();

        mediaController = new MediaController(ViewRecorded.this);
        mediaController .setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(getPath() + intent.getStringExtra("filename"));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();
                videoView.start();
            }
        });
    }

    public String getPath(){
        String path = null;
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            path = Environment.getExternalStorageDirectory() + Helper.DIRECTORY;
        }else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            path = getApplicationContext().getFilesDir() + Helper.DIRECTORY;
        }else{
            path = getApplicationContext().getFilesDir() + Helper.DIRECTORY;
        }
        return path;
    }
}
