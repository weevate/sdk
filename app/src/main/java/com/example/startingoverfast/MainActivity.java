package com.example.startingoverfast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.productactivations.geoadsdk.ActivationsResponse;
import com.productactivations.geoadsdk.DelayedLogger;
import com.productactivations.geoadsdk.EasyLogger;
import com.productactivations.geoadsdk.ProductActivations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1)
                .setFastestInterval(1)
                .setMaxWaitTime(1)
                .setSmallestDisplacement(1);

        return locationRequest;
    }

    int countAction = 0;
    int count = 0;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logsV = findViewById(R.id.log);
        coordinate = findViewById(R.id.coordinate);
        liveAction = findViewById(R.id.live_action);

        ProductActivations.getInstance(getApplicationContext()).initialize(MainActivity.this);

        LocationRequest mLocationRequest = createLocationRequest();

        final FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        final Location mLastLocation = locationResult.getLastLocation();
                        count++;
                        coordinate.setText("Update: " + count+ " Lat: " + mLastLocation.getLatitude() + ", long " + mLastLocation.getLongitude());


                    }
                },
                Looper.myLooper()
        );


        final Handler h = new Handler();

        Runnable run = new Runnable(){
            @Override
            public void run(){

                final String line = EasyLogger.getLiveAction(MainActivity.this);
                MainActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if(line!=null) {
                            setLiveAction(line);
                        }
                        else{

                           setLiveAction("No Update " + (countAction++));
                        }
                    }
                });

                h.postDelayed(this, 4000);
            }
        };

        h.post(run);

    }

    private void setLiveAction(String line){

        liveAction.setText(line);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int i = 0;
        for (String s : permissions) {

            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            EasyLogger.toast(getApplicationContext(), "Permissions granted " + s + " "  + String.valueOf(granted));
            i++;
        }
        ProductActivations.getInstance(getApplicationContext()).onPermissionGranted();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    TextView logsV, coordinate, liveAction;

    public void loadLogs(View view) {


         String[] logs =   EasyLogger.getLogs(getApplicationContext());
        Toast.makeText(getApplicationContext(), "Found " + logs.length + " Logs", Toast.LENGTH_LONG).show();
        if (logs == null) {

            EasyLogger.log("No logs found ");
            return;
        }
        logsV.setText("\r\n\r\n\r\n\r\n");

        for (int i = logs.length - 1; i >= 0; i--) {

            logsV.setText(logsV.getText().toString() + "\r\n\r\n" + logs[i]);
        }
    }

    public void clearLogs(View view) {
        EasyLogger.clearLogs(getApplicationContext());
        loadLogs(null);
    }
}