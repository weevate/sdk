package com.productactivations.geoadsdk;
import io.radar.sdk.Radar;
import io.radar.sdk.RadarTrackingOptions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ProductActivations {

    private static ProductActivations instance = null;

    private Context appContext;

    private int requestCode = 1;
    private String radar_api_key = "prj_live_pk_4fb9d494fd14401117079572640b88ba67819c73";
    private ProductActivations(Context appContext){

        this.appContext = appContext;

    }

    public void initialize(Activity activity, String fcm_token){

        String packageName  = this.appContext.getPackageName();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }


        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        String deviceData = "{\"Platform\":\"android\", \"FcmToken\":\""+fcm_token+"\", \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        Toast.makeText(appContext.getApplicationContext(), deviceData, Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.performPostCall("http://api.productactivations.com/api/v1/geofences/register_device", deviceData);
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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String  performPostCall(String requestURL,
                                   String jsonData) {

        URL url;
        String resp = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");

            conn.setRequestProperty("Accept", "application/json");



            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            conn.setDoInput(true);
            conn.setDoOutput(true);

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                resp = response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resp;
    }



    public void start(){

        Radar.startTracking();
    }



    public static ProductActivations getInstance(Context appContext){
        if(instance==null) instance = new ProductActivations(appContext);
        return instance;
    }
}
