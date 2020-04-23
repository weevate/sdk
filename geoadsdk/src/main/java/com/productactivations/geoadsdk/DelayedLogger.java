package com.productactivations.geoadsdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class DelayedLogger {

    private static DelayedLogger instance;
    private static Context context;

    public static DelayedLogger getInstance(Context mcontext){

        if(instance==null){
            instance = new DelayedLogger();
            context = mcontext;
        }

        return instance;
    }


    public static String STRING_SEPERATOR = "<br/>";
    public static void log(String message){

        EasyLogger.log("INside delayed logger" + message);

        SharedPreferences pref = context.getSharedPreferences("activations", Context.MODE_PRIVATE);
        String existing = pref.getString("logs", null);

        ArrayList<String> lines = null;


        Gson gson = new Gson();
        if(existing==null){

            lines = new ArrayList<String>();
        }
        else{

            String lins = gson.fromJson(existing, String.class);

            lines = (ArrayList<String>) Arrays.asList(lins.split(STRING_SEPERATOR));
        }

        lines.add(message);

        String json = implode(STRING_SEPERATOR, (String[]) lines.toArray());


        SharedPreferences.Editor editPref = pref.edit();
        editPref.putString("logs", json);
        editPref.commit();

    }


    public static ArrayList<String> existingItems(){
        SharedPreferences pref = context.getSharedPreferences("activations", Context.MODE_PRIVATE);
        String existing = pref.getString("logs", null);


        Gson gson = new Gson();
        String lins = gson.fromJson(existing, String.class);

        ArrayList<String> lines;

        if(lins==null) return null;
        lines = (ArrayList<String>) Arrays.asList(lins.split(STRING_SEPERATOR));

        return lines;

    }


    public static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            //data.length - 1 => to not add separator at the end
            if (!data[i].matches(" *")) {//empty string are ""; " "; "  "; and so on
                sb.append(data[i]);
                sb.append(separator);
            }
        }
        sb.append(data[data.length - 1].trim());
        return sb.toString();
    }
}
