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

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class ActivationService extends Service {

    FusedLocationProviderClient mFusedLocationClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        String jsonData = "";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
           // performPostCall("api/v1/geofences/get_geofence", jsonData);
        }

        EasyLogger.toast(this, "Started service ");

        LocationRequest mLocationRequest = LocationRequest.create();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location mLastLocation = locationResult.getLastLocation();

                        EasyLogger.toast(ActivationService.this,  + mLastLocation.getLatitude() + "  long: "+ mLastLocation.getLongitude());

                        EasyLogger.log(" Gotten user loation " + mLastLocation.getLatitude() + "  long: "+ mLastLocation.getLongitude());

                    }
                },
                Looper.myLooper()
        );

        return Service.START_NOT_STICKY;
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