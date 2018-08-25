package com.example.kennck.mirroring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.kennck.mirroring.adapter.VideoAdapter;
import com.example.kennck.mirroring.network.Send;
import com.example.kennck.mirroring.network.WifiBroadcastReceiver;
import com.example.kennck.mirroring.network.WifiDirectMaster;
import com.example.kennck.mirroring.network.WifiDirectServer;
import com.example.kennck.mirroring.objects.Helper;
import com.example.kennck.mirroring.objects.Video;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Account extends AppCompatActivity implements AdapterView.OnItemClickListener{
    Button logout;
    TextView username;
    ListView listView;
    Spinner menuSpinner;
    FloatingActionButton addFile;
    ImageView imageView;


    SharedPreferences sharedpreferences;
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


    ImageSender imageSender;

    ImageReader imageReader;

    final int CONNECTED = 1;
    final  int NOT_CONNECTED = 2;


    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter = new IntentFilter();
    WifiManager wifiManager;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String [] deviceNameArray;
    WifiP2pDevice[] deviceArray ;

    private final int MESSAGE_READ = 1;
    WifiDirectServer wifiDirectServer;
    Send send;

    Boolean imageSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initItems();
        sharedpreferences = getSharedPreferences(Helper.MyPREFERENCES, MODE_PRIVATE);
        username.setText(sharedpreferences.getString("username", null));
        addFile = (FloatingActionButton) findViewById(R.id.addFile);
        menuSpinner = (Spinner) findViewById(R.id.menuSpinner);
        imageView = (ImageView) findViewById(R.id.accountImageView);
        spinnerAdapter = new ArrayAdapter<String>(Account.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        menuSpinner.setAdapter(spinnerAdapter);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        imageReader = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 1);
        initWifiDirect();
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
                    Helper.threadGroup.interrupt();
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
                Helper.threadGroup.interrupt();
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
        Helper.threadGroup.interrupt();
        unregisterReceiver(mReceiver);

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
        /* virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                WIDTH,
                HEIGHT,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),
                null,
                null);
         */
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCaptureViewer",
                WIDTH,
                HEIGHT,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                null);

        imageSender = new ImageSender(Helper.threadGroup, "ImageSender", getApplicationContext());
        try {
            imageSender.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // imageSender.start();
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case CONNECTED:
                    byte[] readBuff = (byte[]) message.obj;
                    String tmpMessage = new String(readBuff, 0, message.arg1);
                    // display tmpMessage
                    listView.setVisibility(View.INVISIBLE);
                    Bitmap bmp = BitmapFactory.decodeByteArray(readBuff, 0, readBuff.length);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bmp);
                    Toast.makeText(Account.this, "Screen Sharing", Toast.LENGTH_LONG).show();
                    break;
                case NOT_CONNECTED:
                    Toast.makeText(Account.this, "Screen Recording", Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }
    });



    public void initWifiDirect(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiverHost(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        discover();
    }

    public void discover(){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Account.this, "Network Discovery Started", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                initWifiDirect();
                Toast.makeText(Account.this, "Network Discovery Starting Failed", Toast.LENGTH_LONG).show();
            }
        });
    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
            }
            if(peers.size() == 0){
                Toast.makeText(Account.this, "Network Device Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                Toast.makeText(Account.this, "Host Connected", Toast.LENGTH_SHORT).show();
                wifiDirectServer = new WifiDirectServer(send, Helper.threadGroup, "Send");
                wifiDirectServer.start();
                // imageSender.start();
            }else if(wifiP2pInfo.groupFormed){
                imageSend = false;
                Toast.makeText(Account.this, "This is a Host and can't be a Client", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public class WifiDirectServer extends Thread {
        Socket socket;
        ServerSocket serverSocket;
        Send send;

        public WifiDirectServer(Send send, ThreadGroup group, String name){
            super(group, name);
            this.send = send;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(Helper.PORT);
                socket = serverSocket.accept();
                send = new Send(socket);
                send.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Send extends Thread{
        private Socket socket;
        private OutputStream outputStream;

        public Send(Socket skt) {
            this.socket = skt;
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // write(null);
        }

        public void write(byte[] bytes){
            try {
                if(bytes != null){
                    outputStream.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    public class ImageSender extends Thread{
        Context context;
        public ImageSender(ThreadGroup threadGroup, String name, Context context){
            super(threadGroup, name);
            this.context = context;
        }

        Handler tHandler = new Handler(Looper.getMainLooper());

        @Override
        public void run() {
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireLatestImage();
                    if(image != null && imageSend == false){
                        imageSend = true;
                        final Image.Plane[] planes = image.getPlanes();
                        final ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * WIDTH;
                        Bitmap bmp = Bitmap.createBitmap(WIDTH+rowPadding/pixelStride, HEIGHT, Bitmap.Config.ARGB_8888);
                        bmp.copyPixelsFromBuffer(buffer);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        send.write(stream.toByteArray());
                        // handler.obtainMessage(CONNECTED, stream.toByteArray()).sendToTarget();
                        Toast.makeText(Account.this, "Sending", Toast.LENGTH_LONG).show();
                        image.close();
                    }
                }
            }, tHandler);
        }
    }
}
