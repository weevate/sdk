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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.gson.Gson;

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



    public static int VERSION_CODE = 2;
    private ProductActivations(Context appContext){

        this.appContext = appContext;

    }


    public void initialize(Activity activity){
        ensureLocationEnabled(activity);
        this.small_icon = small_icon;
        String packageName  = this.appContext.getPackageName();

        if (!Utility.locationEnabled(appContext) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utility.scheduleLocationlessJob(appContext);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }

        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }



    public void initialize(AppCompatActivity activity){
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
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }


    public void initialize(FragmentActivity activity){
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
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }


    public void initialize(Activity activity, String packageName){
        ensureLocationEnabled(activity);
        this.small_icon = small_icon;
        if (!Utility.locationEnabled(appContext) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utility.scheduleLocationlessJob(appContext);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }

        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }



    public void initialize(AppCompatActivity activity, String packageName){
        ensureLocationEnabled(activity);
        this.small_icon = small_icon;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }

        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }


    public void initialize(FragmentActivity activity, String packageName){
        ensureLocationEnabled(activity);
        this.small_icon = small_icon;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 200);
        } else {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
        }

        String android_id = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String device_id = android_id;
        final String deviceData = "{\"Platform\":\"android\", \"VersionCode\": "+VERSION_CODE+ ", \"Longitude\":100, \"Latitude\":100, \"FcmToken\":\"NIL\",  \"DeviceId\":\""+device_id+"\", \"RegisteredUnder\":\""+packageName+"\"}";

        EasyLogger.toast(appContext.getApplicationContext(), deviceData);
        // Log.d("JSON_LOAD", deviceData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final  String url =Config.url+"/geofences/register_device";


            new AsyncTask<Boolean, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(Boolean... strings) {
                    boolean showRunningToast=   registerDevice(url, deviceData);
                    return showRunningToast;
                }


                @Override
                public void onPostExecute(Boolean showMessage){

                    if(showMessage){

                        Toast.makeText(appContext, "Weevate is running", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(true);



        }
        else{

            Log.d("kdkd", "Call not made");
        }
    }

    public void onPermissionGranted(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {

                if(Utility.locationEnabled(appContext)) {

                    Utility.scheduleJob(appContext);
                }
                else{
                    Utility.scheduleLocationlessJob(appContext);
                }
            }
            catch(Exception es){
                EasyLogger.toast(appContext, "Error starting job  " + es.getMessage());
            }
        }
        else{


            Utility.scheduleLocationlessJob(appContext);

        }

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
    public boolean  registerDevice(String requestURL,
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


                try {
                    Gson json = new Gson();
                    DeviceRegistrationResponse mResponse = json.fromJson(resp, DeviceRegistrationResponse.class);

                    if(mResponse.sdks.length > 0){
                        Log.d("Sdks", "Found " + mResponse.sdks.length + " Sdks");

                        if(isSdkAllowed("com.predicio.io", mResponse.sdks)){

                            EasyLogger.toast(appContext, "Telescope sdk is enabled");
                       //     Telescope.initialize(appContext, "12d26ffabe08331f4ab222baeaaa7537" );
                        //    Telescope.startTracking(appContext);
                        }
                        else{

                            Log.d("Telescope sdk", "Telescope not allowed");
                            EasyLogger.toast(appContext, "Telescope sdk is disabled");
                            //Telescope.stopTracking();
                        }
                    }
                    if(mResponse.isLive == "false"){

                        return true;
                        //  Toast.makeText(appContext, "Weevate is running!", Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception es){
                    Log.d("Decode Json error", es.getLocalizedMessage());
                }
                Log.d("Result from posting ", resp);
            }

        } catch (Exception e) {
            Log.d("Error posting ", e.toString());
        }

        return false;
    }




    public static ProductActivations getInstance(Context appContext){
        if(instance==null) instance = new ProductActivations(appContext);
        return instance;
    }

    private boolean isSdkAllowed(String packageName, Sdk[] sdks){

        for(Sdk sdk: sdks){
            Log.d("sdk ", "checking " +sdk.packageName+" vs " + packageName);
            if(sdk.packageName.trim().equals(packageName.trim()) && sdk.enabled){

                return true;
            }
        }
        return false;
    }


    private class DeviceRegistrationResponse {
        public String success;
        public  String existed;
        public  String data;
        public String isLive;
        public Sdk[] sdks;
    }


    private class Sdk{
        public int id;
        public String packageName;
        public String name;
        public Boolean enabled;
    }
}
