package com.example.kennck.mirroring.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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

    }

    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
