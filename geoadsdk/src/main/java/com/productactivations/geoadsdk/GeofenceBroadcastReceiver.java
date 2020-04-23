package com.productactivations.geoadsdk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        sendNotification(context);
        Toast.makeText(context, "Received geofence event", Toast.LENGTH_LONG).show();
        if (geofencingEvent.hasError()) {
            DelayedLogger.getInstance(context).log("Geofence error "+ geofencingEvent.getErrorCode() + " " + geofencingEvent.toString());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();



            for(Geofence geofence : triggeringGeofences){

                Toast.makeText(context, "Triggering geofence is "+geofence.getRequestId(), Toast.LENGTH_LONG).show();
                DelayedLogger.getInstance(context).log("Triggering geofence is " + geofence.getRequestId());
            }
        } else {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
              //      geofenceTransition));
        }
    }


    public void sendNotification(Context context) {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("My notification")
                        .setContentText("Entered Geofence ");


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//


                mNotificationManager.notify(001, mBuilder.build());
    }

}
