package com.example.rr.smartwheelchar;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rr on 17-Mar-16.
 */
public class Util {

    public static double Distance(LatLng a, LatLng b){
        return Math.sqrt( Math.pow(a.latitude -b.latitude, 2) + Math.pow(a.longitude-b.longitude, 2) );
    }

    public static LatLng Middle(LatLng a, LatLng b) {
        return new LatLng((a.latitude + b.latitude)/2, (a.longitude+b.longitude)/2 );
    }

}
