package com.productactivations.geoadsdk;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProductActivations {

    private Context context;
    private Activity activity;

    public void test(Context context, AppCompatActivity activity){

        this.context = context;
        this.activity = activity;

    }



    public void start(){

        Toast.makeText(context, "Working", Toast.LENGTH_LONG).show();
    }
}
