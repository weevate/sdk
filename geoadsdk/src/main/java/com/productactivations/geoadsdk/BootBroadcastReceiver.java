package com.productactivations.geoadsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EasyLogger.toast(context,"Receivd boot");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            EasyLogger.toast(context, "STARTED FROM BOOT");
            Utility.scheduleJob(context); //
        }

        //Toast.makeText(context, "Booted!!!!", Toast.LENGTH_LONG).show();
    }
}
