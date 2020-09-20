package com.ull.emergenciapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class EmergenciAPP extends Application implements LifecycleObserver
{
    private static Context appContext;
    public static boolean isInBackground;

    public static final String LOGIN_PREFERENCES = "preferenciasLogin";
    public static final String NOTIFICATION_TOKEN_PREFERENCES = "notificationToken";
    public static final String USER_DATA_PREFERENCES = "userDataPreferences";
    public static final int USUARIO_GENERAL = 1;
    public static long UPDATE_INTERVAL = 1;

    @Override
    public void onCreate()
    {
        super.onCreate();
        appContext=this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static Context getAppContext() {
        return appContext;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        isInBackground =false;
        restartLocationService();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        isInBackground =true;
        restartLocationService();
    }

    private void restartLocationService(){
        if(LocationService.isRunning) {
            Intent stopLocationService = new Intent(this, LocationService.class);
            stopLocationService.setAction(LocationService.STOP_ACTION);
            stopService(stopLocationService);

            Intent startLocationService = new Intent(this, LocationService.class);
            startLocationService.setAction(LocationService.START_ACTION);

            ContextCompat.startForegroundService(this, startLocationService);
            /*if (isInBackground) {
                ContextCompat.startForegroundService(this, startLocationService);
            } else {
                startService(startLocationService);
            }*/
        }
    }
}