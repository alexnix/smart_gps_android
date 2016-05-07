package com.example.rr.smartwheelchar.bluetooth_specifics;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.rr.smartwheelchar.bluetooth_specifics.CommunicationThread;
import com.example.rr.smartwheelchar.bluetooth_specifics.EventsHandeling;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {
    private BluetoothSocket socket;
    private Activity a;
    private Handler handler;

    public ConnectThread(BluetoothDevice device, Handler handler, Activity a){
        this.handler = handler;
        this.a = a;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            handler.obtainMessage(EventsHandeling.ERROR_RFCOMM).sendToTarget();
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            socket.connect();
            manageConnectedSocket();
        } catch (IOException e) {
            handler.obtainMessage(EventsHandeling.ERROR_CONNECT).sendToTarget();
            e.printStackTrace();
        }
    }

    private void manageConnectedSocket() {
        CommunicationThread com = new CommunicationThread(socket, handler, a);
        handler.obtainMessage(EventsHandeling.SOCKET_CONNECTED, com).sendToTarget();
    }
}
