package com.example.kennck.mirroring;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class SocketHandler extends AsyncTask<String, Void, FileDescriptor> {
    @Override
    protected FileDescriptor doInBackground(String... strings) {
        String URL = strings[0];
        int PORT = 1000;
        Socket socket = new Socket(
                new Proxy(
                        Proxy.Type.SOCKS,
                        new InetSocketAddress(
                                URL,
                                PORT)
                )
        );
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.fromSocket(socket);
        return parcelFileDescriptor.getFileDescriptor();
    }
}
