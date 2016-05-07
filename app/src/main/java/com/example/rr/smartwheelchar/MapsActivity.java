package com.example.rr.smartwheelchar;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.rr.smartwheelchar.bluetooth_specifics.CommunicationThread;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private ArrayList<LatLng> points;
    private CommunicationThread com;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MyApplication app = (MyApplication) getApplicationContext();
        com = app.getCom();

        setUpMapIfNeeded();
    }

    int i = 0;
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                Log.d("Map", "Map clicked" + mMap.getMyLocation().getLatitude() + " " + mMap.getMyLocation().getLongitude());
                marker.setPosition(point);
                String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                        mMap.getMyLocation().getLatitude() + "," + mMap.getMyLocation().getLongitude()
                        + "&destination=" +
                        point.latitude + "," + point.longitude
                        + "&mode=driving";

                Log.d("url", url);
                RequestParams parma = new RequestParams();
                HTTP.get(url, parma, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                            JSONArray stepts = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                            points = new ArrayList<LatLng>();
                            LatLng a = new LatLng(0,0), b = new LatLng(0,0);
                            for (int i = 0; i < stepts.length(); i++) {
                                //LatLng x = getLatLng(stepts.getJSONObject(i));
                                //LatLng y = getLatLng(stepts.getJSONObject(i - 1));
                                a = new LatLng(
                                        stepts.getJSONObject(i).getJSONObject("start_location").getDouble("lat"), stepts.getJSONObject(i).getJSONObject("start_location").getDouble("lng")
                                );
                                options.add(a);

                                b = new LatLng(
                                        stepts.getJSONObject(i).getJSONObject("end_location").getDouble("lat"), stepts.getJSONObject(i).getJSONObject("end_location").getDouble("lng")
                                );
                                options.add(b);
                                points.add(a);
                            }
                            points.add(b);

                            mMap.addPolyline(options);
                            process();

                            final Handler hnd = new Handler() {
                                public void handleMessage(Message msg) {
                                    if ( msg.what == 101 ) {
                                        move_to();
                                    }
                                }
                            };
                            com.handler = new android.os.Handler(){
                                @Override
                                public void handleMessage(Message msg) {
                                    if ( msg.what == 0 ) {
                                        Message m = new Message();
                                        m.what = 101;
                                        hnd.sendMessage(m);
                                    }
                                }
                            };

                            com.start();


                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e("failure", responseString + "CODE:" + statusCode);
                    }
                });
            }
        });
    }

    int corner_idx = 0, step_idx = 0;
    String direction;
    public void move_to(){
        if( total_steps.size() > step_idx )
            marker.setPosition(total_steps.get(step_idx));

        if( corners.size() > corner_idx ) {
            double d = dist(total_steps.get(step_idx), corners.get(corner_idx).center);
            Log.i("Distance to corner", d +"");
            if( d < 7 ) {
                if( corners.get(corner_idx).orientation == 1 ) {
                    direction = "DREAPTA";
                    com.write("3");
                }
                else {
                    direction = "STANGA";
                    com.write("2");
                }

                Toast.makeText(this, direction, Toast.LENGTH_SHORT ).show();
                if( d < 1 ) {
                    corner_idx++;
                    com.write("4");
                }
            }
        }

        step_idx++;
    }

    ArrayList<LatLng> total_steps = new ArrayList<LatLng>();
    ArrayList<Corner> corners = new ArrayList<Corner>();
    ArrayList<LatLng> steps ;
    private void process(){
        double m1 = 123.123, m2 = 123.123;

        LatLng x = new LatLng(0,0), y = new LatLng(0,0), z;
        for(int i = 0; i < points.size(); i++) {
            if( i > 0 ) {
                m1 = m2;
                x = points.get(i);
                y = points.get(i-1);

                steps = new ArrayList<LatLng>();
                calc_intre(x, y);
                sort_steps(y);
                total_steps.addAll(steps);

                m2 = (y.longitude - x.longitude != 0)? (y.latitude - x.latitude) / (y.longitude - x.longitude): 99999;
            }

            if(m1 != 123.123) {
                double angle = Math.abs( (m1 - m2) / (1 - m1*m2) );
                if(angle > 0.8){
                    x = points.get(i);
                    y = points.get(i-1);
                    z = points.get(i-2);
                    //mMap.addMarker(new MarkerOptions().position(y));
                    int orientation;
                    Log.wtf("Numbers one by one", x.latitude + " " + x.longitude + " " + y.latitude + " " + y.longitude + " " + z.latitude + " " + z.longitude);
                    double orienatation = 9;
                    orienatation = Math.signum(((y.latitude - z.latitude) * (x.longitude - z.longitude) - (y.longitude - z.longitude) * (x.latitude - z.latitude)));
                    Log.i("Corner orientation", orienatation + "");
                    corners.add(new Corner(
                            y,
                            (int)orienatation
                    ));
                }
            }
        }
    }
    private void calc_intre(LatLng a, LatLng b) {
        if( Util.Distance(a, b) > 0.000007){
            LatLng m = Util.Middle(a, b);
            steps.add(m);
            calc_intre(a, m);
            calc_intre(b, m);
        }
    }

    private double dist(LatLng a, LatLng b) {
        return Math.sqrt( Math.pow((a.latitude - b.latitude), 2) + Math.pow((a.longitude - b.longitude),2) ) * 100000;
    }

    private void sort_steps(LatLng cp) {
        final LatLng y = cp;
        Collections.sort(steps, new Comparator<LatLng>() {
            @Override
            public int compare(LatLng a, LatLng b) {
                if(Util.Distance(y, a) - Util.Distance(y, b) > 0)
                    return 1;
                else
                    return -1;
            }
        });
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);

        Location location = mMap.getMyLocation();

        if (location != null) {
            Log.wtf("we_have_pos","yayaya");
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14));
        } else Log.wtf("no_pos", "nononono");

    }
}
