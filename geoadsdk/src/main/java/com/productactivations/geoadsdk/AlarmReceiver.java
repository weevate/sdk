package com.productactivations.geoadsdk;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            EasyLogger.log("REceived alarm");
            Context oAppContext = context.getApplicationContext();

            if (oAppContext == null) {
                oAppContext = context;
            }
            Intent serviceIntent = new Intent(oAppContext, ActivationService.class);
            oAppContext.startService(serviceIntent);
        }
    }

