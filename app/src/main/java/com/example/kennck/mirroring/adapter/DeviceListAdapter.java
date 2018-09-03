package com.example.kennck.mirroring.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kennck.mirroring.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>{
    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> arrayList;
    private int mViewResourceId;

    public DeviceListAdapter(@NonNull Context context, int tvResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, tvResourceId, devices);
        this.arrayList = devices;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        convertView = layoutInflater.inflate(mViewResourceId, null);
        BluetoothDevice device = arrayList.get(position);

        if(device != null){
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);

            if(deviceName != null){
                deviceName.setText(device.getName());
            }
            if(deviceAddress != null){
                deviceAddress.setText(device.getAddress());
            }
        }else{
            //
        }
        return convertView;
    }
}
