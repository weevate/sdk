package com.productactivations.geoadsdk;
import io.radar.sdk.Radar;
import io.radar.sdk.RadarTrackingOptions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ProductActivations {

    private static ProductActivations instance = null;

    private Context appContext;

    private int requestCode = 1;
    private String radar_api_key = "prj_live_pk_4fb9d494fd14401117079572640b88ba67819c73";
    private ProductActivations(Context appContext){

        this.appContext = appContext;

    }

    public void initialize(Activity activity){

        String packageName  = this.appContext.getPackageName();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);

        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }




    }


    public void onPermissionGranted(){

        Radar.initialize("prj_live_pk_4fb9d494fd14401117079572640b88ba67819c73");

        RadarTrackingOptions trackingOptions = new RadarTrackingOptions.Builder()
                .priority(Radar.RadarTrackingPriority.RESPONSIVENESS) // use EFFICIENCY instead to reduce location update frequency
                .offline(Radar.RadarTrackingOffline.REPLAY_STOPPED) // use REPLAY_OFF instead to disable offline replay
                .sync(Radar.RadarTrackingSync.POSSIBLE_STATE_CHANGES) // use ALL instead to sync all location updates
                .build();

        Radar.startTracking();
        Toast.makeText(appContext.getApplicationContext(), "Started tracking", Toast.LENGTH_LONG).show();

    }

    public void start(){

        Radar.startTracking();
    }



    public static ProductActivations getInstance(Context appContext){
        if(instance==null) instance = new ProductActivations(appContext);
        return instance;
    }
}
