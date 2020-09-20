package com.ull.emergenciapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Response;

import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class LocationService extends Service {
    public static final String START_ACTION = "START_SERVICE";
    public static final String STOP_ACTION = "STOP_SERVICE";
    private static final String CHANNEL_ID = "locationServiceChannel";
    private static final String TAG = "mylog";

    public static boolean isRunning = false;

    private static Location lastLocation;
    private Location newLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mlocationCallback;
    //private final static long UPDATE_INTERVAL = 1000 * 60;  /* 1 min */
    //private final static long FASTEST_INTERVAL = UPDATE_INTERVAL / 2; /* 30 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: got location result.");
                newLocation = locationResult.getLastLocation();
                int id = getUserID();

                if (newLocation != null) {
                    Log.d(TAG, "Lat: " + newLocation.getLatitude() + ", Long: " + newLocation.getLongitude() + ", Accuracy: " + newLocation.getAccuracy());
                    if (isBetterLocation(newLocation, lastLocation)) {
                        lastLocation = newLocation;
                        saveUserLocation(id, newLocation);
                    }
                }
            }
        };

        startNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == START_ACTION) {
            Log.d(TAG, "onStartCommand: called. Iniciando servicio");
            getLocation();
            isRunning = true;
        } else if (intent.getAction() == STOP_ACTION) {
            Log.d(TAG, "onStartCommand: called. Deteniendo servicio");
            isRunning = false;
            stopService(new Intent(this, LocationService.class));
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Deteniendo servicio");
        mFusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

    private void getLocation() {
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        if (EmergenciAPP.isInBackground) { // check if app is in background
            Log.d("mylog", "Servicio de localizacion en ahorro de energia");
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60) * 2);
            mLocationRequest.setFastestInterval((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60));
            //startNotification();
        } else {
            Log.d("mylog", "Servicio de localizacion en máxima precisión");
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60));
            mLocationRequest.setFastestInterval((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60)/2);
        }

        Log.d(TAG, "getLocation: getting location information.");

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mlocationCallback, Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void startNotification(){
        // Configuramos la notificación para Android Oreo o superior
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }

        Intent stopnotificationIntent = new Intent(this, LocationService.class);
        stopnotificationIntent.setAction(STOP_ACTION);
        PendingIntent intent = PendingIntent.getService(this, 0, stopnotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationId = new Random().nextInt(60000);

        // Creamos la notificación en si
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.logo))
                .setContentTitle("EmergenciAPP se está ejecutando")
                .setContentText("Toca para detener")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("\nObteniendo ubicación...\nToca para detener"))
                //.setContentIntent(intent)
                .setSound(null)
                .setWhen(0)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(0, "Detener", intent);

        startForeground(notificationId, notificationBuilder.build());
    }

    private void saveUserLocation(final int id, final Location userLocation){
        if(id != -1){
            //if(checkNewLocation(id, userLocation)) {
                UserService userService = APIEngine.getAPIEngine().create(UserService.class);
                Call<Response<String>> call = userService.setLocation(
                        RequestBody.create(String.valueOf(id), MediaType.parse("text/plain")),
                        RequestBody.create(String.valueOf(userLocation.getLatitude()), MediaType.parse("text/plain")),
                        RequestBody.create(String.valueOf(userLocation.getLongitude()), MediaType.parse("text/plain"))
                );
                call.enqueue(new Callback<Response<String>>() {
                    @Override
                    public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                        if (response.isSuccessful()) {
                            Response<String> resp = response.body();
                            if (resp.getResult()) {
                                Log.d(TAG, "Ubicación actualizada correctamente");
                            } else {
                                // TODO Reset lastLocation
                                Log.d(TAG, "Fallo al actualizar ubicación");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<String>> call, Throwable t) {
                        // TODO Reset lastLocation
                        Log.d(TAG, "Fallo al actualizar ubicación - ERROR: " + t.getMessage());
                    }
                });
            //}
        }else{
            Log.d(TAG, "No se ha obtenido un id válido aún");
        }
    }

    private int getUserID(){
        SharedPreferences loginPreferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        return loginPreferences.getInt("id", -1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        String channelName = "Location Service Channel";
        String channelDescription = "Notificación para informar de que la aplicación se está ejecutando en segundo plano, enviando la ubicación actual";
        NotificationChannel channel;
        channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(channelDescription);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(null, null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Determines whether one Location reading is better than the current Location fix
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60) * 4);
        boolean isSignificantlyOlder = timeDelta < -((EmergenciAPP.UPDATE_INTERVAL * 1000 * 60) * 4);
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static Location getLastLocation(){
        return lastLocation;
    }
}


