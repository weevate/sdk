package com.productactivations.geoadsdk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GeoService {

    Context context;



    FusedLocationProviderClient mFusedLocationClient;
    private GeofencingClient geofencingClient ;


    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }




    private static final String TAG = "SyncService";

    public GeoService(Context context){

        EasyLogger.toast(context, "called geoservice");
        this.context = context;
    }


    public void doJob(){
        String jsonData = "";

        EasyLogger.toast(context, "Started service ");

        LocationRequest mLocationRequest = createLocationRequest();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        final Location mLastLocation = locationResult.getLastLocation();


                        String json = "{ \n" +
                                "\"Latitude\":\""+mLastLocation.getLatitude()+"\",\n" +
                                "\"Longitude\":\""+mLastLocation.getLongitude()+"\",\n" +
                                "\"DeviceId\":\"484848484848\"\n" +
                                "}";


                        new doPostRequest(){

                            @Override
                            public void onPreExecute(){


                            }


                            @Override
                            public void onPostExecute(String result){


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



    }



    private int GEOFENCE_EXPIRES_IN = 1000 * 60 * 30;

    public ActivationsResponse stringToResponse(String result){

        try {
            // Toast.makeText(ActivationService.this, " Parsing ", Toast.LENGTH_LONG).show();
            Gson gson = new Gson();
            ActivationsResponse response = gson.fromJson(result, ActivationsResponse.class);

            EasyLogger.toast(context, "Finished parsing, found " + response.data.length + " nearby geofences");
            // Toast.makeText(ActivationService.this, "Finished parsing " + response.data.length, Toast.LENGTH_LONG).show();
            return response;
        }
        catch(Exception es){
            EasyLogger.log("error parsing location " + es.toString());
            EasyLogger.toast(context, "Exception getting location " + es.toString());
            //   Toast.makeText(getApplicationContext(), "Exception getting location " + es.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private boolean alreadyInGeofence(PLocation location){


        SharedPreferences prefs  = context.getSharedPreferences("geofences", Context.MODE_PRIVATE);

        int lastEnteredId = prefs.getInt("last_geofence_id", -1);

        EasyLogger.toast(context, "Last registered geofence id" + lastEnteredId + ". This one is " + location.id);
        // Toast.makeText(getApplicationContext(), "Last entered " + lastEnteredId + " vs " + location.id, Toast.LENGTH_LONG).show();
        return (lastEnteredId == location.id);


    }



    private boolean setGeofence(PLocation location){


        SharedPreferences prefs  = context.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("last_geofence_id", location.id);

        editPrefs.commit();

        return true;


    }



    private boolean removeGeofence(PLocation location){


        SharedPreferences prefs  = context.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("last_geofence_id", -1);
        EasyLogger.toast(context, "left geofence");
        editPrefs.commit();

        return true;


    }

    public boolean inRadius(PLocation geofence, Location currentLocation){

        Log.d("calc distance to " + geofence.name ,  geofence.latitude + ", long" + geofence.longitude + " vs " + currentLocation.getLatitude() + ": " + currentLocation.getLongitude());


        geofence.radius = 50;

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

        EasyLogger.toast(context, "Current Radius to location ("+geofence.name+") is "  + dist + " metres while required: <" + geofence.radius);
        EasyLogger.saveForViewing(context, "Current Radius to location ("+geofence.name+") is "  + dist + " metres while required: <" + geofence.radius + " (Checked against " + " lat" + currentLocation.getLatitude() +", lng "+ currentLocation.getLongitude()+")");

        boolean result = dist <= (float) geofence.radius;

        EasyLogger.toast(context, "In Geofence? "+String.valueOf(result));

        return result;
    }


    String CHANNEL_ID = "ads";
    String CHANNEL_NAME = "productactivations";

    private void sendNotification(SdkNotification notification){
        try {
            new SendNotification(context, notification).execute("");
        }
        catch(Exception es){

            EasyLogger.toast(context, es.getMessage());
        }

    }

    public void registerNotifications(ActivationsResponse response,  Location currentLocation){


        PLocation closest = response.data[0];

        if(closest.notifications.length < 1){

            EasyLogger.toast(context, "This geofence has no notifications attached");
            return;

        }

        if(!inRadius(closest, currentLocation)){


            //cancel already displayed notification
            if(alreadyInGeofence(closest)){
                EasyLogger.toast(context, "Exited geofence " + closest.name);
                SendNotification nm = new SendNotification(context, null);
                int displayedNotification = nm.getPendingNotificationId();
                if(displayedNotification!=-1)
                    nm.cancelNotification(displayedNotification);
                nm.saveNotificationId(-1);
                removeGeofence(closest);
            }


            return;
        }

        if(alreadyInGeofence(closest)){

            EasyLogger.toast(context, "Already in geofence");
            return;
        }

        setGeofence(closest);
        sendNotification(closest.notifications[0]);


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
}
