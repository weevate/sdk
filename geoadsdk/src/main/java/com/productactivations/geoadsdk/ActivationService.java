package com.productactivations.geoadsdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class ActivationService extends Service {

    FusedLocationProviderClient mFusedLocationClient;
    private GeofencingClient geofencingClient ;


    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        String jsonData = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
           // performPostCall("api/v1/geofences/get_geofence", jsonData);
        }

        EasyLogger.toast(this, "Started service ");

        LocationRequest mLocationRequest = createLocationRequest();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        final Location mLastLocation = locationResult.getLastLocation();

                        EasyLogger.toast(ActivationService.this,  "Your location: lat  " + mLastLocation.getLatitude() + ",  long: "+ mLastLocation.getLongitude());

                        String json = "{ \n" +
                                "\"Latitude\":\""+mLastLocation.getLatitude()+"\",\n" +
                                "\"Longitude\":\""+mLastLocation.getLongitude()+"\",\n" +
                                "\"DeviceId\":\"484848484848\"\n" +
                                "}";



                        new doPostRequest(){

                            @Override
                            public void onPreExecute(){
                           //     Toast.makeText(getApplicationContext(), "About to start ", Toast.LENGTH_LONG).show();

                            }


                            @Override
                            public void onPostExecute(String result){
                           //     Toast.makeText(getApplicationContext(), "finished making request "+result, Toast.LENGTH_LONG).show();

                               if(result!=null && result.indexOf("data") > 0 ){

                                    ActivationsResponse response  = stringToResponse(result);
                                    registerNotifications(response, mLastLocation);
                                }
                            }
                        }.execute("https://api.productactivations.com/api/v1/geofences/get_geofences",json);

                    }
                },
                Looper.myLooper()
        );

        //Toast.makeText(getApplicationContext(), "returning sticky ", Toast.LENGTH_LONG).show();

        return Service.START_NOT_STICKY;
    }



    private int GEOFENCE_EXPIRES_IN = 1000 * 60 * 30;

    public ActivationsResponse stringToResponse(String result){

        try {
           // Toast.makeText(ActivationService.this, " Parsing ", Toast.LENGTH_LONG).show();
            Gson gson = new Gson();
            ActivationsResponse response = gson.fromJson(result, ActivationsResponse.class);

            EasyLogger.toast(this, "Finished parsing, found " + response.data.length + " nearby geofences");
           // Toast.makeText(ActivationService.this, "Finished parsing " + response.data.length, Toast.LENGTH_LONG).show();
            return response;
        }
        catch(Exception es){
            EasyLogger.log("error parsing location " + es.toString());
            EasyLogger.toast(this, "Exception getting location " + es.toString());
        //   Toast.makeText(getApplicationContext(), "Exception getting location " + es.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private boolean alreadyInGeofence(PLocation location){


        SharedPreferences prefs  = this.getApplicationContext().getSharedPreferences("geofences", Context.MODE_PRIVATE);

        int lastEnteredId = prefs.getInt("last_geofence_id", -1);

        EasyLogger.toast(getApplicationContext(), "Last registered geofence id" + lastEnteredId + ". This one is " + location.id);
       // Toast.makeText(getApplicationContext(), "Last entered " + lastEnteredId + " vs " + location.id, Toast.LENGTH_LONG).show();
        return (lastEnteredId == location.id);


    }



    private boolean setGeofence(PLocation location){


        SharedPreferences prefs  = this.getApplicationContext().getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("last_geofence_id", location.id);

        editPrefs.commit();

        return true;


    }



    private boolean removeGeofence(PLocation location){


        SharedPreferences prefs  = this.getApplicationContext().getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("last_geofence_id", -1);
        EasyLogger.toast(getApplicationContext(), "left geofence");
        editPrefs.commit();

        return true;


    }

    public boolean inRadius(PLocation geofence, Location currentLocation){

        Log.d("calc distance to " + geofence.name ,  geofence.latitude + ", long" + geofence.longitude + " vs " + currentLocation.getLatitude() + ": " + currentLocation.getLongitude());
         if(geofence.radius < 100){
            geofence.radius = 100;
        }

        double lat1 = geofence.latitude;
        double lng1 = geofence.longitude;

        double lat2 = currentLocation.getLatitude();
        double lng2 = currentLocation.getLongitude();

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        EasyLogger.toast(this, "Current Radius to location ("+geofence.name+") is "  + dist + " metres while required: <" + geofence.radius);
        Log.d("Distance is " , "Dist is " + dist + " vs " + (float) geofence.radius);
        boolean result = dist <= (float) geofence.radius;

        EasyLogger.toast(this, "In Geofence? "+String.valueOf(result));

        return result;
    }


    String CHANNEL_ID = "ads";
    String CHANNEL_NAME = "productactivations";

    private void sendNotification(SdkNotification notification){
        try {
        //    new SendNotification(this, notification, this).execute("");
        }
        catch(Exception es){

            EasyLogger.toast(this, es.getMessage());
        }

    }

    public void registerNotifications(ActivationsResponse response,  Location currentLocation){


      // sendNotification(response.data[0].notifications[0]);
        PLocation closest = response.data[0];

        if(closest.notifications.length < 1){


                EasyLogger.toast(this, "This geofence has no notifications attached");
       //         registerNotifications(response, currentLocation, count);
                return;

        }

        if(!inRadius(closest, currentLocation)){
            if(alreadyInGeofence(closest)){
                EasyLogger.toast(this, "Exited geofence " + closest.name);
                removeGeofence(closest);
            }


            return;
        }

        if(alreadyInGeofence(closest)){

            EasyLogger.toast(getApplicationContext(), "Already in geofence");
            return;
        }

        setGeofence(closest);
        sendNotification(closest.notifications[0]);


        /*
       ArrayList<Geofence> geofenceList = getGeofences(response);
        if(geofenceList.size() < 1){
            EasyLogger.log("No notifications set");
            return;
        }
       geofencingClient.addGeofences(getGeofenceingRequest(geofenceList), getGeofencePendingIntent()).addOnSuccessListener(new Executor() {
           @Override
           public void execute(Runnable command) {
               EasyLogger.log("Inside executor");
           }
       }, new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void aVoid) {
               Toast.makeText(ActivationService.this, "Success adding", Toast.LENGTH_LONG).show();
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
               if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                   Toast.makeText(ActivationService.this, "GPS Provider not avaialeble" + e.toString(), Toast.LENGTH_LONG).show();
               }
               if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                   Toast.makeText(ActivationService.this, "Network provider not avaialeble" + e.toString(), Toast.LENGTH_LONG).show();
               }
               Toast.makeText(ActivationService.this, "failure adding " + e.toString(), Toast.LENGTH_LONG).show();
           }
       }).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               Toast.makeText(ActivationService.this, "Geofence completed " , Toast.LENGTH_LONG).show();
           }
       });
        Toast.makeText(ActivationService.this, "Added geofence", Toast.LENGTH_LONG).show();
*/
    }


    PendingIntent geofencePendingIntent;

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public GeofencingRequest getGeofenceingRequest(ArrayList<Geofence> geofenceList){

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();


    }
    public  ArrayList<Geofence> getGeofences(ActivationsResponse newR){

        geofencingClient = LocationServices.getGeofencingClient(ActivationService.this);

        ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();


        for(PLocation loc : newR.data){

            if(loc.radius < 100){
                loc.radius = 100;
            }

            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(String.valueOf(loc.id))

                    .setCircularRegion(
                            loc.latitude,
                            loc.longitude,
                            (float) loc.radius
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRES_IN)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(180000)
                    .build());
        }

        return geofenceList;
    }


    class doPostRequest extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return performPostCall(strings[0], strings[1]);
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result){


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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}