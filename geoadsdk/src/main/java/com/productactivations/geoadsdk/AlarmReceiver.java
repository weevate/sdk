package com.productactivations.geoadsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EasyLogger.toast(context, "Received alarm");
        Intent i2 = new Intent(context, ActivationService.class);
        context.startService(i2);
    }
}
