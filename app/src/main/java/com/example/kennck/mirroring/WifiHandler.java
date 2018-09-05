package com.example.kennck.mirroring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kennck.mirroring.objects.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WifiHandler extends AppCompatActivity {
    public Button back;
    public TextView connectStatus;
    public ListView listView;
    public ImageView imageView;
    private final String TAG = "WIFIP2P";

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pDevice[] deviceArray ;

    WifiManager wifiManager;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String [] deviceNameArray;

    Client client;
    Receive receive;
    Send send;
    boolean receiveFlag = false;
    Test test;

    final int MESSAGE_READ = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_handler);
        initialize();
        listenerBtns();
    }

    public void initialize() {
        back = (Button) findViewById(R.id.wHBack);
        listView = (ListView)findViewById(R.id.whListView);
        connectStatus = (TextView)findViewById(R.id.wifiStatus);
        imageView = (ImageView)findViewById(R.id.imageViewer);
        initWifiDirect();
        // test = new Test();
        // test.start();
    }

    public void initWifiDirect(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiverSlave(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        discover();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) message.obj;
                    // String tmpMessage = new String(readBuff, 0, message.arg1);
                    // display tmpMessage
                    listView.setVisibility(View.INVISIBLE);
                    connectStatus.setVisibility(View.INVISIBLE);
                    Bitmap bmp = BitmapFactory.decodeByteArray(readBuff, 0, readBuff.length);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bmp);
                    receiveFlag = true;
                    String text = "1";
                    send.write(text.getBytes());
                    Log.d(TAG, "HANLDER FOR IMAGE EXECUTED");
                    break;
            }
            return false;
        }
    });

    public class Test extends  Thread{
        String filename;
        String directory;
        public Test(){
            String path;
            String state = Environment.getExternalStorageState();
            if(Environment.MEDIA_MOUNTED.equals(state)){
                path = Environment.getExternalStorageDirectory() + "/DCIM/CAMERA/";
            }else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
                path = getApplicationContext().getFilesDir() + "/DCIM/CAMERA/";
            }else {
                path = getApplicationContext().getFilesDir() + "/DCIM/CAMERA/";
            }
            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdir();
            }else{
                //
            }
            directory = path;
            filename = "IMG_20180827_073621.jpg";
        }

        @Override
        public void run() {
            File image = new File(directory,filename);
            try {
                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(image));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                handler.obtainMessage(MESSAGE_READ,stream.toByteArray()).sendToTarget();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Helper.threadGroup.interrupt();
    }

    public void discover(){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiHandler.this, "Network Discovery Started", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                initWifiDirect();
                Toast.makeText(WifiHandler.this, "Network Discovery Starting Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void listenerBtns(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.threadGroup.interrupt();
                Intent back  = new Intent(WifiHandler.this, Account.class);
                startActivity(back);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiHandler.this, "Connected to " + device.deviceName, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(WifiHandler.this, "Not Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }
            if(peers.size() == 0){
                Toast.makeText(WifiHandler.this, "Network Device Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                Toast.makeText(WifiHandler.this, "This is a Client Side not Host Side", Toast.LENGTH_SHORT).show();
            }else if(wifiP2pInfo.groupFormed){
                Log.d(TAG, "CLIENT CONNECTED");
                Toast.makeText(WifiHandler.this, "Client Connected", Toast.LENGTH_SHORT).show();
                client = new Client(groupOwnerAddress, Helper.threadGroup, "Client Thread");
                client.start();
            }
        }
    };

    public class Client extends Thread {
        Socket socket;
        String hostAddress;

        public Client(InetAddress hostAddress, ThreadGroup threadGroup, String name){
            super(threadGroup, name);
            this.hostAddress = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "CLIENT THREAD STARTED");
                socket.connect(new InetSocketAddress(hostAddress, Helper.PORT), Helper.TIME_OUT);
                receive = new Receive(socket, Helper.threadGroup, "Receive");
                receive.start();
                send = new Send(socket);
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
                Log.d(TAG,"SEND THREAD STARTED ON CLIENT SIDE");
                if(bytes != null && receiveFlag == true){
                    receiveFlag = false;
                    outputStream.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class Receive extends Thread{
        private Socket socket;
        private InputStream inputStream;

        public Receive(Socket skt, ThreadGroup group, String name) {
            super(group, name);
            this.socket = skt;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "RECEIVE THREAD STARTED ON CLIENT SIDE");
            byte[] buffer = new byte[1024 * 1000];
            int bytes;
            if (socket != null) {
                Log.d(TAG, "WHILE LOOP ON IMAGE ON CLIENT SIDE");
                try {
                    Log.d(TAG, "TRY CATCH ON IMAGE ON CLIENT SIDE");
                    bytes = inputStream.read();
                    if (bytes > 0) {
                        Log.d(TAG, "RECEIVE IMAGE ON CLIENT SIDE");
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
