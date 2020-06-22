package com.productactivations.geoadsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class EasyLogger {

    public static String TAG = "productactivations";


    public static void log(String message){

        Log.d(TAG, message);
    }

    public static void toast(Context context, String message){
        //save(message, context);
        //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void saveForViewing(Context context, String message){

    /*    String line = getDateNow() + ": " + message;
        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);


        SharedPreferences.Editor pref = preferences.edit();
        pref.putString("display", line);
        pref.commit();
 */
    }


    public static String getLiveAction(Context context){

        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);
        String existing = preferences.getString("display", null);

        return existing;
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

        String[] existingArray = existing.split("<br/>");

        if(existingArray.length > 41) {
            existingArray = Arrays.copyOfRange(existingArray, (existingArray.length-1)-40, existingArray.length-1);
            existing = TextUtils.join("<br/>", existingArray);
        }

        SharedPreferences.Editor pref = preferences.edit();
        pref.putString("logs", existing);
        pref.commit();

    }



    public static void resetLog(Context context){
        SharedPreferences preferences =  context.getSharedPreferences("productactivations", Context.MODE_PRIVATE);
        String existing = null;

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

        String[] result = existing.split("<br/>");
        String[] finalResult = result;
        if(result.length > 41){

            finalResult = Arrays.copyOfRange(result, (result.length-1)-40, result.length-1);
            resetLog(context);
        }

        return finalResult;




    }


    public static class getLogsAsync extends AsyncTask<Void, Void, String[]> {

        Context context;
        public getLogsAsync(Context context){
            super();
            this.context = context;
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            String[] result = getLogs(context);

            return result;
        }

        public void onExecute(String[] result){

        }
    }




}
