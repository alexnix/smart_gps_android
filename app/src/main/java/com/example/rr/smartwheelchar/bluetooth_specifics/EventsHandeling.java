package com.example.rr.smartwheelchar.bluetooth_specifics;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.example.rr.smartwheelchar.MapsActivity;
import com.example.rr.smartwheelchar.MyApplication;

public class EventsHandeling extends android.os.Handler {

    public static final int SOCKET_CONNECTED = 1;
    public static final int DATA_RECEIVED = 2;
    public static final int ERROR_CONNECT = 3, ERROR_COMMUNICATION = 4, ERROR_RFCOMM = 5;
    public static final int ERROR_OBTAIN_STREAMS = 6;

    private Snackbar snackbar;
    private Activity activity;
    private MyApplication app;
    private int count;

    public EventsHandeling(Snackbar snackbar, Activity activity){
        this.snackbar = snackbar;
        this.activity = activity;
        this.app = (MyApplication) activity.getApplicationContext();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SOCKET_CONNECTED: {
                snackbar.setText("Socket connectat").show();

                MyApplication app = (MyApplication) activity.getApplicationContext();
                app.setCom((CommunicationThread)msg.obj);

//                ((CommunicationThread)msg.obj).start();
//                ((CommunicationThread)msg.obj).write("salutare gigele");

                this.activity.startActivity(new Intent(this.activity.getApplicationContext(), MapsActivity.class));


                break;
            }

            case DATA_RECEIVED: {
                break;
            }

            case ERROR_CONNECT: {
                snackbar.setText("Eroare de conectare").show();
                break;
            }

            case ERROR_COMMUNICATION: {
                snackbar.setText("Eroare la comunicare").show();
                break;
            }

            case ERROR_RFCOMM: {
                snackbar.setText("Eroare la deschiderea canalului").show();
                break;
            }

            case ERROR_OBTAIN_STREAMS: {
                snackbar.setText("Eroare in obtinrea strea-urilor").show();
                break;
            }
        }
    }
}
