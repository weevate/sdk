package com.productactivations.geoadsdk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GeoJobService extends JobService {

        private static final String TAG = "SyncService";

        @Override
        public boolean onStartJob(JobParameters params) {
            Intent service = new Intent(getApplicationContext(), ActivationService.class);
            getApplicationContext().startService(service);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utility.scheduleJob(getApplicationContext()); // reschedule the job
            }
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return true;
        }

    }

