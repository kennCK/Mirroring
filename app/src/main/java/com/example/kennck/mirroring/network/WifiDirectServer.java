package com.example.kennck.mirroring.network;

import com.example.kennck.mirroring.objects.Helper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
