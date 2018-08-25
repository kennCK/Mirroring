package com.example.kennck.mirroring.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.kennck.mirroring.Account;
import com.example.kennck.mirroring.objects.Helper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiDirectMaster {
    Context context;
    Account account;
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
    public Send send;

    public boolean status = false;

    public WifiDirectMaster(Account account){
        this.account = account;
        context = account.getApplicationContext();
        initWifiDirect();
    }

    public void initWifiDirect(){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this, account, null);
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
                Toast.makeText(context, "Network Discovery Started", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                initWifiDirect();
                status = false;
                Toast.makeText(context, "Network Discovery Starting Failed", Toast.LENGTH_LONG).show();
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
                Toast.makeText(context, "Network Device Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                Toast.makeText(context, "Host Connected", Toast.LENGTH_SHORT).show();
                status = true;
                wifiDirectServer = new WifiDirectServer(send, Helper.threadGroup, "Send");
                wifiDirectServer.start();
            }else if(wifiP2pInfo.groupFormed){
                Toast.makeText(context, "This is a Host and can't be a Client", Toast.LENGTH_SHORT).show();
            }
        }
    };

}
