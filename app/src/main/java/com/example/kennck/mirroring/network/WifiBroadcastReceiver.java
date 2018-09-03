package com.example.kennck.mirroring.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.example.kennck.mirroring.Account;
import com.example.kennck.mirroring.WifiHandler;

public class WifiBroadcastReceiver extends BroadcastReceiver  {
    private final String TAG = "BroadcastReceiver";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectMaster wifiDirect;
    private Account activity;
    private WifiHandler wifiHandler;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectMaster wifiDirect, Account activity, WifiHandler wifiHandler) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.wifiDirect = wifiDirect;
        this.activity = activity;
        this.wifiHandler = wifiHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "onReceive: WIFI P2P Enabled");
            } else {
                Log.d(TAG, "onReceive: WIFI Disabled to Enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, wifiDirect.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: Connection Changed");
            if(mManager == null){
                return;
            }
            NetworkInfo networkInfo =  intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel, wifiDirect.connectionInfoListener);
            }else{
                if(wifiHandler == null){
                    Toast.makeText(activity, "Network Device Disconected", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(wifiHandler, "Network Device Disconnected", Toast.LENGTH_LONG).show();
                }
            }
        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "onReceive: Device Change State");
        }
    }
}