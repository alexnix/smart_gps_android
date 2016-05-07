package com.example.rr.smartwheelchar.bluetooth_specifics;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by m17336 on 2/5/2016.
 */
public class CommunicationThread extends Thread {
    private BluetoothSocket socket;
    public Handler handler;
    private InputStream is;
    private OutputStream os;

    private Activity a;

    int cont = 0;

    public CommunicationThread(BluetoothSocket socket, Handler handler, Activity a) {
        this.socket = socket;
        this.a = a;
        this.handler = handler;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            handler.obtainMessage(EventsHandeling.ERROR_OBTAIN_STREAMS).sendToTarget();
        }
    }

    public void run(){
        byte[] buffer = new byte[1024];
        int bytes;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while( true ){
            try {
                String data = br.readLine();
                //String data = new String(buffer, 0, bytes);
                //if(data.startsWith("SCA"))
                Log.wtf("data_red", data);

                try {
                    Message m = new Message();
                    m.what = Integer.parseInt(data);
                    handler.sendMessage(m);
                } catch (Exception e) {}
            } catch (IOException e){
                handler.obtainMessage(EventsHandeling.ERROR_COMMUNICATION).sendToTarget();
            }
        }
    }

    public void write(String message){
        String m = message;
        byte[] msgBuffer = m.getBytes();
        try {
            os.write(msgBuffer);
        } catch (IOException e) {}
    }
}
