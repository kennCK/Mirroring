package com.example.kennck.mirroring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
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

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Account extends AppCompatActivity implements AdapterView.OnItemClickListener{
    Button logout;
    TextView username;
    SharedPreferences sharedpreferences;
    FloatingActionButton addFile;
    ArrayAdapter adapter;
    ArrayAdapter <String> spinnerAdapter;
    ListView listView;
    Spinner menuSpinner;
    final int FILE_REQUEST = 1;
    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    VirtualDisplay virtualDisplay;
    final int RECORDCODEREQUEST = 500;
    MediaRecorder mediaRecorder;
    private final int WIDTH = 720;
    private final int HEIGHT = 1280;
    int screenDensity;
    String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        logout = (Button)findViewById(R.id.accLogout);
        username = (TextView) findViewById(R.id.accUsername);
        listView = (ListView) findViewById(R.id.recordList);
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, MODE_PRIVATE);
        username.setText(sharedpreferences.getString("username", null));
        addFile = (FloatingActionButton) findViewById(R.id.addFile);
        menuSpinner = (Spinner) findViewById(R.id.menuSpinner);
        spinnerAdapter = new ArrayAdapter<String>(Account.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        menuSpinner.setAdapter(spinnerAdapter);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        filepath = null;
        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Intent askCode = new Intent(Account.this, CheckCode.class);
                    startActivity(askCode);
                }else if(position == 2){
                    askRecordPermission();
                }else if(position == 3){
                    stopRecording();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addFileButton();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                sharedpreferences.edit().clear().commit();
                Intent login = new Intent(Account.this, Login.class);
                startActivity(login);
            }
        });
        retrieveRecords();
        listView.setOnItemClickListener(this);
    }


    @Override
    protected void onDestroy() {
        if(mediaRecorder != null){
            stopRecording();
        }
        super.onDestroy();
    }

    public void askRecordPermission(){
        if(mediaProjection == null){
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), RECORDCODEREQUEST);
        }else{
            Log.v("Recording:", "Stop");
        }
    }

    private void addFileButton() {
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFile();
            }
        });
    }

    private void selectFile() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FILE_REQUEST && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
        }else if(requestCode == RECORDCODEREQUEST && resultCode == RESULT_OK && data != null){
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            initMediaRecorder();
        }
    }

    public void fileUpload(Uri url){
        RequestQueue queue = Volley.newRequestQueue(Account.this);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = (TextView)view;
        Toast.makeText(this, "Code: " + tv.getText() + position, Toast.LENGTH_LONG).show();
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
        prepareMediaRecorder();
    }

    public void getFilePath(){
        File f;
        do{
            String filename = sharedpreferences.getString("username", null) + "_" + (System.currentTimeMillis()) + ".mp4";
            filepath += "/Recorder/" + filename;
            f = new File(filepath);
        }while(f.exists() && !f.isDirectory());
    }

    public String getFilePathDirectory(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording/";
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
