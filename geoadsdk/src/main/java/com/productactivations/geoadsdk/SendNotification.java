package com.productactivations.geoadsdk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendNotification  extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String message;
        SdkNotification notification;

        public SendNotification(Context context, SdkNotification notification) {
            super();
            this.ctx = context;
            this.notification = notification;
        }


        String host_url = "https://productactivations.com";
        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
           // message = params[0] + params[1];
            try {

                URL url = new URL(host_url+notification.icon);
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

    private void sendNotification(Bitmap largeIcon){

        NotificationCompat.Builder mBuilder;
        NotificationManager mNotificationManager;

        mBuilder = new NotificationCompat.Builder(ctx);
        mBuilder.setSmallIcon(R.drawable.logo);
        mBuilder.setContentTitle(notification.subject)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(largeIcon))
                .setContentText(notification.message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

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
        mNotificationManager.notify(100 /* Request Code */, mBuilder.build());

    }


    @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
                EasyLogger.log("result is " + result);
                sendNotification(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


}
