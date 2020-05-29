package com.productactivations.geoadsdk;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class Utility {

    // schedule the start of the service every 10 - 30 seconds
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, GeoJobService.class);

        //save boot time so we can know when the device is rebooted
        saveBootTime(context);

        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);

        builder.setMinimumLatency(1000*60 * 3); // wait at least
        builder.setOverrideDeadline(1000 * 60 * 6); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(builder.build());
        }

        scheduleForceRestartOnBoot(context);
    }


    //schedule boottime check with firebase so if the device is rebooted, the service still works



    private static void saveBootTime(Context context){

        long bootTime = android.os.SystemClock.elapsedRealtime();

        SharedPreferences prefs = context.getSharedPreferences("geosdk", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPref = prefs.edit();
        editPref.putLong("last_boot_time", bootTime);
        editPref.commit();
    }


    //watch for reboot (for chinese phones) and force start the service
    public static void scheduleForceRestartOnBoot(Context context){

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));



        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(GeoBootService.class)
                // uniquely identifies the job
                .setTag("geoadasdk")
                // repeat the job
                .setRecurring(true)
                // persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 5 minutes from now
                .setTrigger(Trigger.executionWindow(0, 60))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();

        dispatcher.mustSchedule(myJob);
        EasyLogger.toast(context, "Scheduled force start on boot");

    }


    public static void scheduleWithWorkManager(Context myContext){


        Constraints constraints = new Constraints.Builder()
                // The Worker needs Network connectivity
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request =
                // Executes MyWorker every 15 minutes
                new PeriodicWorkRequest.Builder(GeoWorkManager.class, 10, TimeUnit.MINUTES).setConstraints(constraints)
                        .build();

        WorkManager.getInstance(myContext)
                // Use ExistingWorkPolicy.REPLACE to cancel and delete any existing pending
                // (uncompleted) work with the same unique name. Then, insert the newly-specified
                // work.
                .enqueueUniquePeriodicWork("geo-worker", ExistingPeriodicWorkPolicy.REPLACE, request);

    }

}
