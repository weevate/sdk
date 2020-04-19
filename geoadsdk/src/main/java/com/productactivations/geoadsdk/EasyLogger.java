package com.productactivations.geoadsdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


public class EasyLogger {

    public static String TAG = "Remcred";


    public static void log(String message){

        Log.d(TAG, message);
    }

    public static void toast(Context context, String message){

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }






}
