package com.example.rr.smartwheelchar;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rr on 17-Mar-16.
 */
public class Corner {
    public LatLng center;
    public int orientation;
    public Corner(LatLng center, int o){
        this.center = center;
        this.orientation = o;
    }
}
