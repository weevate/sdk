package com.productactivations.geoadsdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.ALARM_SERVICE;

public class ProductActivations {

    private static ProductActivations instance = null;

    private Context appContext;

    private int requestCode = 1;
    private String radar_api_key = "prj_live_pk_4fb9d494fd14401117079572640b88ba67819c73";
    private ProductActivations(Context appContext){

        this.appContext = appContext;

    }



    public boolean AppHasRunBefore(){

        SharedPreferences preferences  = appContext.getSharedPreferences("geoadsdk", Context.MODE_PRIVATE);
        boolean runBefore = preferences.getBoolean("runBefore", false);
        return runBefore;

    }


    public void updateRunStatus(){

        SharedPreferences preferences  = appContext.getSharedPreferences("geoadsdk", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPref = preferences.edit();
        editPref.putBoolean("runBefore", true);
        editPref.apply();
        editPref.commit();

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
        final String deviceData = "{\"Platform\":\"android\", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\""+fcm_token+"\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url ="https://api.productactivations.com/api/v1/geofences/register_device";


            new AsyncTask<String, String, String>(){

                @Override
                protected String doInBackground(String... strings) {
                    performPostCall(url, deviceData);
                    return null;
                }
            }.execute("");



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }


    public void onPermissionGranted(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Utility.scheduleJob(appContext);
                if(!AppHasRunBefore()) {
                    updateRunStatus();
                    EasyLogger.toast(appContext, "started first time");
                    Utility.scheduleWithWorkManager(appContext);
                }
                else{

                    EasyLogger.toast(appContext, "Skipping start. App already run before");
                }
            }
            catch(Exception es){
                EasyLogger.toast(appContext, "Error starting job  " + es.getMessage());
            }
            EasyLogger.toast(appContext,"Started scheduler");
        }


        }




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String  performPostCall(String requestURL,
                                   String jsonData) {

        Log.d("performing call", "performing call");

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





            conn.setDoInput(true);
            conn.setDoOutput(true);



            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                resp = response.toString();

                Log.d("Result from posting ", resp);
            }

        } catch (Exception e) {
            Log.d("Error posting ", e.toString());
        }

        return resp;
    }



    public static ProductActivations getInstance(Context appContext){
        if(instance==null) instance = new ProductActivations(appContext);
        return instance;
    }
}
