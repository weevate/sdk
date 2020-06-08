package com.productactivations.geoadsdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
    private int small_icon;
    private ProductActivations(Context appContext){

        this.appContext = appContext;

    }

    public void initialize(Activity activity, String fcm_token){
        ensureLocationEnabled(activity);
        this.small_icon = small_icon;
        String packageName  = this.appContext.getPackageName();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }

        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        final String deviceData = "{\"Platform\":\"android\", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

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
            }
            catch(Exception es){
                EasyLogger.toast(appContext, "Error starting job  " + es.getMessage());
            }
            EasyLogger.toast(appContext,"Started scheduler");
        }



        /*   Intent i2 = new Intent(appContext, ActivationService.class);

            appContext.startService(i2);

            EasyLogger.log("Started service" );
            AlarmManager am = (AlarmManager)this.appContext.getSystemService(ALARM_SERVICE);
            Intent i = new Intent(this.appContext, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getService(appContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar t = Calendar.getInstance();
            t.setTimeInMillis(System.currentTimeMillis());

            int interval =  120000;
            am.setRepeating(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), interval, pendingIntent);
           // EasyLogger.log("Started alarm"); */

        }



    public void ensureLocationEnabled(final Activity activity){
        LocationManager lm = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(activity)
                    .setMessage("To get the best out of this app, please enable locations")
                    .setPositiveButton("Enable Location", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                            .setNegativeButton("Cancel",null)
                            .show();
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
