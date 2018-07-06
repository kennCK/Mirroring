package com.example.kennck.mirroring;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Account extends AppCompatActivity implements AdapterView.OnItemClickListener{
    Button logout;
    TextView username;
    ListView listView;
    Spinner menuSpinner;
    FloatingActionButton addFile;


    SharedPreferences sharedpreferences;
    ArrayAdapter adapter;
    ArrayAdapter <String> spinnerAdapter;

    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    VirtualDisplay virtualDisplay;


    MediaRecorder mediaRecorder;
    final int RECORDCODEREQUEST = 500;
    private final int WIDTH = 720;
    private final int HEIGHT = 1280;
    private final String DIRECTORY = Helper.DIRECTORY;
    int screenDensity;

    VideoAdapter videoAdapter;
    List<Video> videoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initItems();
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, MODE_PRIVATE);
        username.setText(sharedpreferences.getString("username", null));
        addFile = (FloatingActionButton) findViewById(R.id.addFile);
        menuSpinner = (Spinner) findViewById(R.id.menuSpinner);
        spinnerAdapter = new ArrayAdapter<String>(Account.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        menuSpinner.setAdapter(spinnerAdapter);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        newRecordingListener();
        retrieveLocalRecordings();
        btnListeners();

    }

    public void initItems(){
        logout = (Button)findViewById(R.id.accLogout);
        username = (TextView) findViewById(R.id.accUsername);
        listView = (ListView) findViewById(R.id.recordList);
    }

    public void btnListeners(){

        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Intent wifi = new Intent(Account.this, WifiHandler.class);
                    startActivity(wifi);
                }else if(position == 2){
                    stopRecording();
                    // stopRTCRecording();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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


        listView.setOnItemClickListener(this);
    }


    @Override
    protected void onDestroy() {
        if(mediaRecorder != null){
            stopRecording();
        }
        super.onDestroy();
    }

    private void newRecordingListener() {
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askRecordPermission();
            }
        });
    }

    public void askRecordPermission(){
        if(mediaProjection == null){
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), RECORDCODEREQUEST);
        }else{
            Log.v("Recording:", "Stop");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RECORDCODEREQUEST && resultCode == RESULT_OK && data != null){
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            initMediaRecorder();
        }
    }

    public void retrieveLocalRecordings(){
        File file = new File(getPath());
        videoList = new ArrayList<Video>();
        if (file.isDirectory()) {
            File[] fileArr = file.listFiles();
            int length = (fileArr != null) ? fileArr.length : 0;
            for (int i = 0; i < length; i++) {
                File f = fileArr[i];
                videoList.add(new Video(getPath(),  f.getName()));
            }
            videoAdapter = new VideoAdapter(this, videoList);
            // adapter = new ArrayAdapter<String>(this, R.layout.content_text_view, videosString);
            listView.setAdapter(videoAdapter);
        }else{
            // displayRecordsEmpty("No Recordings Found");
        }
    }

    public String getPath(){
        String path = null;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            path = Environment.getExternalStorageDirectory() + DIRECTORY;
        }else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            path = getApplicationContext().getFilesDir() + DIRECTORY;
        }else {
            path = getApplicationContext().getFilesDir() + DIRECTORY;
        }
        return path;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent recorded = new Intent(Account.this, ViewRecorded.class);
        Video details = (Video) parent.getItemAtPosition(position);
        recorded.putExtra("filename", details.getFilename());
        startActivity(recorded);
    }

    public void initMediaRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setVideoSize(WIDTH, HEIGHT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setOutputFile(getFilePathDirectory());

        // mediaRecorder.setOutputFile(String.valueOf(new SocketHandler().execute("http://mirroring.classworx.co/public")));
        prepareMediaRecorder();
    }

    public String getFilePathDirectory(){
        String path = getPath();
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdir();
        }else{
            //
        }
        String filename = sharedpreferences.getString("username", null) + "_" + (System.currentTimeMillis()) + ".mp4";
        return path + filename;
    }

    public void prepareMediaRecorder(){
        try {
            mediaRecorder.prepare();
            Log.v("Recording:", "Preparing");
            mediaRecorder.start();
            Log.v("Recording:", "Started");
            startRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        if(mediaRecorder != null){
            mediaProjection.stop();
            mediaRecorder.release();
            virtualDisplay.release();
            virtualDisplay = null;
            mediaRecorder = null;
        }
    }

    public void startRecording(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenDensity = metrics.densityDpi;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                WIDTH,
                HEIGHT,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),
                null,
                null);
    }
}
