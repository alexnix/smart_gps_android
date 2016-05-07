package com.example.rr.smartwheelchar;

import android.app.Application;

import com.example.rr.smartwheelchar.bluetooth_specifics.CommunicationThread;

/**
 * Created by m17336 on 2/12/2016.
 */
public class MyApplication extends android.app.Application {
    private CommunicationThread com;

    public CommunicationThread getCom() {
        return com;
    }

    public void setCom(CommunicationThread com) {
        this.com = com;
    }
}
