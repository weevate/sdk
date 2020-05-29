package com.productactivations.geoadsdk;

import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GeoJobService extends JobService {


        public boolean onStartJob(JobParameters params) {

            GeoService service = new GeoService(getApplicationContext());
            service.doJob();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utility.scheduleJob(getApplicationContext()); // reschedule the job
            }
            return true;
        }

        public boolean onStopJob(JobParameters params) {
            return true;
        }





    }

