package com.productactivations.geoadsdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GeoWorkManager extends ListenableWorker {


    private Context context;
    public GeoWorkManager(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
        context = appContext;
    }

    @Override
    public ListenableFuture<Result> startWork() {
        // Do your work here.
        Data input = getInputData();

        EasyLogger.toast(context,"Started from work manager");
        new GeoService(context).doJob();


        // Return a ListenableFuture<>
        return new MResult();
    }


    class MResult implements ListenableFuture {


        @Override
        public void addListener(Runnable listener, Executor executor) {

        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() throws ExecutionException, InterruptedException {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return null;
        }
    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
    }
}
