package com.productactivations.geoadsdk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class SendNotification  extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String message;
        SdkNotification notification;
        SdkNotificationResultListener  service;


        public SendNotification(Context context, SdkNotification notification, SdkNotificationResultListener service) {
            super();
            this.ctx = context;
            this.notification = notification;
            this.service = service;

        }

        public SendNotification(Context context, SdkNotification notification) {
            super();
            this.ctx = context;
            this.notification = notification;

        }

        String host_url = Config.url;
        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
           // message = params[0] + params[1];
            try {

                String m_url = notification.icon.indexOf("http") > -1? notification.icon:  host_url+notification.icon;
                EasyLogger.toast(ctx, "fetching image " + m_url);
                URL url = new URL(m_url);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    String CHANNEL_ID = "ads";
    String CHANNEL_NAME = "productactivations";


    private void savePendingUrlToPreferences(){
        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("pending_notification_id", notification.id);
        editPrefs.putString("pending_package_name", ctx.getPackageName());
        editPrefs.commit();
    }

    public void saveNotificationId(int id ){

        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("pending_notification_id", id);
        editPrefs.commit();

    }


    public int getPendingNotificationId( ){

        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        int id = prefs.getInt("pending_notification_id", -1);
        return id;

    }

    //@Todo there is no need to fetch the bitmap from the server first before checking if notification has been delivered before
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(Bitmap largeIcon){

        if(hasNotBeenDeliveredToday(notification.id)){

            EasyLogger.toast(ctx, "Cancellilng delivery this note was delivered recently");
            service.onNotificationNotSent();
            return;
        }

        if(!shouldWeDeliverThisNoteNow()){


            EasyLogger.toast(ctx, "Cancellilng delivery: not upto an hour since last delivery");
            service.onNotificationNotSent();
            return;
        }

        EasyLogger.toast(ctx, "Not cancelling notification");

        saveDeliveredNotification(notification.id);
        saveLastDeliveryTime();

        //Intent notificationIntent = new Intent(ctx, WebViewActivity.class);
        String url = notification.url!=null? notification.url: Config.url+"geofences/performed_click/"+notification.id+"/"+ctx.getPackageName()+"/"+ProductActivations.VERSION_CODE;

        String delivery_url = Config.url+"geofences/performed_delivery/"+notification.id+"/"+ctx.getPackageName()+ "/"+ProductActivations.VERSION_CODE;

        new doGetRequest(){

            @Override
            public void onPreExecute(){
                EasyLogger.toast(ctx, "About to report delivery");
            }


        }.execute(delivery_url);

        EasyLogger.toast(ctx, "Url posted to is " + url);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));


//**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 500,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
       // savePendingUrlToPreferences();

        NotificationCompat.Builder mBuilder;
        NotificationManager mNotificationManager;

        mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.weevate_36_36);
        mBuilder.setContentTitle(notification.subject)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(largeIcon))
                .setContentText(notification.message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(contentIntent);

        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        Notification notif = mBuilder.build();
        notif.flags = Notification.FLAG_AUTO_CANCEL;

        int id = new Random().nextInt(100);
        //EasyLogger.toast(ctx, "Flashed notification"+id);
        saveNotificationId(id);
        mNotificationManager.notify(id /* Request Code */, notif);
        service.onNotificationSent();

    }


    //save the time this notification was displayed to avoid displaying again till a day has passed
    public void saveDeliveredNotification(int not_id){

        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        String key = "n"+not_id;
        String value = "t"+System.currentTimeMillis();

        EasyLogger.toast(ctx, "Saving " + key + " with value  as delivered. Will not deliver again till 24 hrs");
        editPrefs.putString(key, value);
        editPrefs.commit();
    }

    //save the time this notification was displayed to avoid displaying again till a day has passed
    public void saveLastDeliveryTime(){

        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPrefs = prefs.edit();
        String key = Config.DELIVERY_TIME_KEY;
        String value = "t"+System.currentTimeMillis();

        EasyLogger.toast(ctx, "Saving " + key + " with value  as delivered. Will not deliver again till 1 hrs");
        editPrefs.putString(key, value);
        editPrefs.commit();
    }




    private boolean shouldWeDeliverThisNoteNow(){

        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);

        String key = Config.DELIVERY_TIME_KEY;
        String last_note_delivery_time = prefs.getString(key, null);

        EasyLogger.toast(ctx, "Checking " + key + "  found " + last_note_delivery_time);

        if(last_note_delivery_time == null) {

            EasyLogger.toast(ctx, "checking if its too early to deliver note: NO previous record of note found");
            return true;
        }

        long timeDelivered = Long.valueOf(last_note_delivery_time.replace("t", ""));

        EasyLogger.toast(ctx, "Long value  " + timeDelivered);
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - timeDelivered;

        EasyLogger.toast(ctx, "Time passed in millis " + timeElapsed);

        int hoursPassed = (int) (((timeElapsed/1000)/60)/60);

        //int minutesPassed  = (int) (((timeElapsed/1000)));


        boolean shouldDeliver = hoursPassed > 0;

        EasyLogger.toast(ctx, "hours passed since last notification was delivered " + hoursPassed);

        //EasyLogger.toast(ctx, "Has note been delivered today? " + String.valueOf(hasBeenDelivered));
        return shouldDeliver;
    }

    //save the time this notification was displayed to avoid displaying again till a day has passed
    private boolean hasNotBeenDeliveredToday(int not_id){

        EasyLogger.toast(ctx, "Checking not_id " + not_id);
        SharedPreferences prefs  = ctx.getSharedPreferences("geofences", Context.MODE_PRIVATE);

        String key = "n"+not_id;
        String not_delivery_time = prefs.getString(key, null);

        EasyLogger.toast(ctx, "Checking " + key + "  found " + not_delivery_time);

        if(not_delivery_time == null) {
            EasyLogger.toast(ctx, "Not has not been delivered before");
            return false;
        }

        long timeDelivered = Long.valueOf(not_delivery_time.replace("t", ""));

        EasyLogger.toast(ctx, "Long value  " + timeDelivered);
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - timeDelivered;

        EasyLogger.toast(ctx, "Time passed in millis " + timeElapsed);

        int hoursPassed = (int) (((timeElapsed/1000)/60)/60);

        //int minutesPassed  = (int) (((timeElapsed/1000)));


        boolean hasBeenDelivered =  hoursPassed < 24;

        EasyLogger.toast(ctx, "hours passed since note was delivered " + hoursPassed);

        //EasyLogger.toast(ctx, "Has note been delivered today? " + String.valueOf(hasBeenDelivered));
        return hasBeenDelivered;
    }


    public void cancelNotification(int id){

        if(id==-1){
            return;
        }

       // NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.ca ncel(id);
        //EasyLogger.toast(ctx, "Cleard notification " + id);

    }

    @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
               // EasyLogger.log("result is " + result);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sendNotification(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        //Anti Pattern
        class doGetRequest extends AsyncTask<String, String, String> {


            @Override
            protected String doInBackground(String... strings) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return performGetCall(strings[0]);
                }

                return null;
            }


            @Override
            protected void onPostExecute(String result){

                EasyLogger.toast(ctx, "Result from registering " + result);

            }
        }




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String  performGetCall(String requestURL) {


        URL url;
        String resp = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Accept", "application/json");





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
            EasyLogger.toast(ctx, "Error makign request " +e.toString());
        }

        return resp;
    }
}



