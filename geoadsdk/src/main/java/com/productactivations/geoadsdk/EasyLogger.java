package com.productactivations.geoadsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;


public class EasyLogger {

    public static String TAG = "productactivations";


    public static void log(String message){

        Log.d(TAG, message);
    }

    public static void toast(Context context, String message){
        save(message, context);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    public static void clearLogs(Context context){

        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);
        String existing = null;

        SharedPreferences.Editor pref = preferences.edit();
        pref.putString("logs", existing);
        pref.commit();
    }


    public static String getDateNow(){

        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        return currentDate;
    }

    public static void save(String line, Context context){
        line = getDateNow() + ": " + line;
        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);
        String existing = preferences.getString("logs", null);

        if(existing==null){

            existing = line;
        }
        else{

            existing = existing+ "<br/>" + line;
        }

        SharedPreferences.Editor pref = preferences.edit();
        pref.putString("logs", existing);
        pref.commit();

    }


    public static String[] getLogs(Context context){

        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);
        String existing = preferences.getString("logs", null);

        if(existing == null){
            return new String[]{};
        }

        return existing.split("<br/>");
    }





}
