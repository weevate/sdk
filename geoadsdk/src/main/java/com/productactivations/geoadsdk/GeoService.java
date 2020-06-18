package com.productactivations.geoadsdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class GeoService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful

        EasyLogger.toast(this, "Started geoservice");
        SdkService sdkService= new SdkService(this);
        sdkService.doJob();
        return Service.START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
