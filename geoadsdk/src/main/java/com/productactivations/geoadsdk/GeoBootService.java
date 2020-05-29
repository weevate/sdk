package com.productactivations.geoadsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.firebase.jobdispatcher.JobParameters;

public class GeoBootService extends com.firebase.jobdispatcher.JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        EasyLogger.toast(getApplicationContext(), "Onbootservice start job");
        if(deviceHasBeenRebooted(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utility.scheduleJob(getApplicationContext()); // reschedule the job
            }
        }

        return false;
    }

    private boolean deviceHasBeenRebooted(Context context){

        EasyLogger.toast(context, "Checking device rebooted");
        long lastBootTimeSaved = getLastBootTime(context);

        long timeElapsedSinceBootAccToSdk = java.lang.System.currentTimeMillis() -lastBootTimeSaved;
        long timeElapsedSinceBootAccToPhone = java.lang.System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime();

         if(timeElapsedSinceBootAccToPhone < timeElapsedSinceBootAccToSdk){

             EasyLogger.toast(context, "device was rebooted");
             return true;
         }


        EasyLogger.toast(context, "device not rebooted");
         return false;
    }





    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }



    private static long getLastBootTime(Context context){

        SharedPreferences prefs = context.getSharedPreferences("geosdk", Context.MODE_PRIVATE);
        return prefs.getLong("last_boot_time", -1);
    }
}
