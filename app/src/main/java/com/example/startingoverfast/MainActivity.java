package com.example.startingoverfast;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.productactivations.geoadsdk.DelayedLogger;
import com.productactivations.geoadsdk.EasyLogger;
import com.productactivations.geoadsdk.ProductActivations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logsV = findViewById(R.id.log);

        ProductActivations.getInstance(getApplicationContext()).initialize(MainActivity.this, "NO NORE FCM");

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
            Toast.makeText(getApplicationContext(), "Permissions granted " + s + " " + grantResults[i] + " " + String.valueOf(granted), Toast.LENGTH_LONG).show();
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

    TextView logsV;

    public void loadLogs(View view) {


        String[] logs = EasyLogger.getLogs(getApplicationContext());
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