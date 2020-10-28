package com.productactivations.geoadsdk;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;

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
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationLessService extends JobService implements SdkNotificationResultListener {



    FusedLocationProviderClient mFusedLocationClient;
    private GeofencingClient geofencingClient ;
    private JobParameters params;

    long REQUEST_INTERVAL_MILLISECONDS = 6000;

    long lastRequestTime = System.currentTimeMillis();




        private static final String TAG = "SyncService";

        @Override
        public boolean onStartJob(JobParameters params) {
            preventCrashes();
            this.params = params;
            EasyLogger.toast(getApplicationContext(), "Started execution of locationless job");
            doJob();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Utility.locationEnabled(getApplicationContext())) {
                Utility.scheduleLocationlessJob(getApplicationContext()); // reschedule the job
            }
            return true;
        }




        private void preventCrashes(){
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    System.exit(2);
                }
            });
        }

        @Override
        public boolean onStopJob(JobParameters params)
        {
            return true;
        }




        ActivationsResponse nearbyNotifications;
        Location mLastLocation;

        private void doJob(){
            String jsonData = "";


                            //EasyLogger.toast(GeoJobService.this,  " Requested geofence: Your location: lat  " + mLastLocation.getLatitude() + ",  long: "+ mLastLocation.getLongitude());
                            String packageName = getApplicationContext().getPackageName();


                            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);


                            String json = "{ \n" +
                                    "\"Latitude\":\"-1\",\n" +
                                    "\"Longitude\":\"-1\",\n" +
                                    "\"DeviceId\":\""+android_id+"\",\n" +
                                    "\"PackageName\":\""+packageName+"\" }";

                            //EasyLogger.toast(getApplicationContext(), "Content : " + json);
                            new doPostRequest(){

                                @Override
                                public void onPreExecute(){
                                    //     Toast.makeText(getApplicationContext(), "About to start ", Toast.LENGTH_LONG).show();

                                    EasyLogger.toast(getApplicationContext(), "Requested locationless geofences");
                                }



                                @Override
                                public void onPostExecute(String result){
                                    //     Toast.makeText(getApplicationContext(), "finished making request "+result, Toast.LENGTH_LONG).show();

                                    if(result!=null && result.indexOf("data") > 0 ){
                                        EasyLogger.toast(getApplicationContext(),  "Result geofences " +result.length());
                                        nearbyNotifications  = stringToResponse(result);

                                        nearbyNotifications =  treatNotifications(nearbyNotifications);
                                        registerNotifications(nearbyNotifications, mLastLocation);
                                    }
                                }
                            }.execute("https://api.productactivations.com/api/v1/geofences/get_geofences",json);

        }


        //closest location notiications with locationless notifications
        private ActivationsResponse treatNotifications(ActivationsResponse nearbyNotifications){

            List<SdkNotification> notifications = new ArrayList<SdkNotification>();

            notifications.addAll(Arrays.asList(nearbyNotifications.data[0].notifications));


            nearbyNotifications.data[0].notifications = new SdkNotification[notifications.size()];

            int counter = 0;
            for(SdkNotification notification: notifications){
                nearbyNotifications.data[0].notifications[counter++] = notification;
            }

             return nearbyNotifications;
        }

    private int GEOFENCE_EXPIRES_IN = 1000 * 60 * 30;

    public ActivationsResponse stringToResponse(String result){

        try {
            // Toast.makeText(ActivationService.this, " Parsing ", Toast.LENGTH_LONG).show();
            Gson gson = new Gson();
            ActivationsResponse response = gson.fromJson(result, ActivationsResponse.class);

           // EasyLogger.toast(this, "Finished parsing, found " + response.data.length + " nearby geofences");
            // Toast.makeText(ActivationService.this, "Finished parsing " + response.data.length, Toast.LENGTH_LONG).show();
            return response;
        }
        catch(Exception es){
            EasyLogger.log("error parsing location " + es.toString());
                }
        return null;
    }




    String CHANNEL_ID = "ads";
    String CHANNEL_NAME = "productactivations";

    private void sendNotification(SdkNotification notification){
        try {
          SendNotification sendNotification =  new SendNotification(this, notification, this);
          sendNotification.execute("");

        }
        catch(Exception es){

            EasyLogger.toast(this, es.getMessage());
        }

    }



    int attemptsToSendNotification = 0;
    public void registerNotifications(ActivationsResponse response,  Location currentLocation){

        if(response.data.length < 1) return;
        PLocation closest = response.data[0];
        EasyLogger.toast(getApplicationContext(), " Closest " + closest.name+ " has " + closest.notifications.length);

        if(closest.notifications.length < 1){
            EasyLogger.toast(this, "This geofence has no notifications attached ");
            //         registerNotifications(response, currentLocation, count);
            return;
        }



        EasyLogger.toast(getApplicationContext(), "Attempt to send notifiation is " + attemptsToSendNotification);
        if(closest.notifications.length > attemptsToSendNotification) {
            sendNotification(closest.notifications[attemptsToSendNotification]);
        }
        else{
            attemptsToSendNotification = 0;
        }


    }


    public void registerNextNotification(ActivationsResponse response,  Location currentLocation){

        PLocation closest = response.data[0];

        EasyLogger.toast(getApplicationContext(), "delivering notification " + attemptsToSendNotification);
        sendNotification(closest.notifications[attemptsToSendNotification]);

    }

    public void finishJob(){
        EasyLogger.toast(getApplicationContext(), "Finished job");
        this.jobFinished(this.params, true);
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

      @Override
    public void onNotificationNotSent(){

        EasyLogger.toast(getApplicationContext(), "Notification not sent " + nearbyNotifications.data[0].notifications.length);
        attemptsToSendNotification+=1;

        EasyLogger.toast(getApplicationContext(), "Attempts " + attemptsToSendNotification + ", length "+ nearbyNotifications.data[0].notifications.length);
        if(nearbyNotifications.data[0].notifications.length > attemptsToSendNotification){
            EasyLogger.toast(getApplicationContext(), "Delivered this note before; unto the next");
            registerNextNotification(nearbyNotifications, mLastLocation);
        }        

        else{
            finishJob();
            EasyLogger.toast(getApplicationContext(), "No more locationless notes");
        }
    }


    public void onNotificationSent(){

        finishJob();
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

            catch(Exception es){
                EasyLogger.toast(getApplicationContext(), "Error reading result " + es.getMessage());
            }
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                resp = response.toString();

            }

        } catch (Exception e) {
            EasyLogger.toast(getApplicationContext(), "Error makign request " +e.toString());
        }

        return resp;
    }
    }

