package com.ull.emergenciapp.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ull.emergenciapp.AceptarSolicitud;
import com.ull.emergenciapp.EmergenciAPP;
import com.ull.emergenciapp.Login;
import com.ull.emergenciapp.R;
import com.ull.emergenciapp.SolicitudRecibida;
import com.ull.emergenciapp.Splash;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String ALERT_RESPONSE ="Response";
    public static final String ALERT_SEND = "Alert";
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID ="notificationChannel";
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("NEW_TOKEN","TOKEN_: " + token);
        saveRegistrationToken(token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("mylog", "Nueva notificación");
        Intent notificationIntent;
        String a = remoteMessage.getData().get("action");
        switch (remoteMessage.getData().get("action")){
            case ALERT_SEND:
                if(EmergenciAPP.isInBackground){
                    Log.d("mylog", "Notificación de tipo ALERT en background");
                    notificationIntent = new Intent(this, Splash.class);

                    //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent intent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // Configuramos la notificación para Android Oreo o superior
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        setupChannels();
                    }

                    int notificationId = new Random().nextInt(60000);

                    //TODO Modificar para añadir pantalla de aceptación
                    // Creamos la notificación en si
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                                    R.drawable.logo))
                            .setContentTitle(remoteMessage.getData().get("title"))
                            .setContentText(remoteMessage.getData().get("message"))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("message") + "\n(" + remoteMessage.getData().get("timestamp") + ")"))
                            .setAutoCancel(true)  //dismisses the notification on click
                            .setContentIntent(intent)
                            .setSound(defaultSoundUri)
                            .setPriority(NotificationManager.IMPORTANCE_MAX)
                            .setWhen(0);
                    //.addAction(R.drawable.check, "Aceptar", intent);
                    //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notificationId, notificationBuilder.build());
                }else{
                    Log.d("mylog", "Notificación de tipo ALERT");
                    if(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getBoolean("logged", false)) {
                        Log.d("mylog", "Usuario logueado");
                        notificationIntent = new Intent(this, AceptarSolicitud.class);
                    }else{
                        Log.d("mylog", "Usuario NO logueado");
                        notificationIntent = new Intent(this, Login.class);
                        notificationIntent.setAction("Redirect");
                        notificationIntent.putExtra("redirect", "aceptarSolicitud");
                    }
                    for (Map.Entry<String, String> pair : remoteMessage.getData().entrySet()) {
                        notificationIntent.putExtra(pair.getKey(), pair.getValue());
                    }
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(notificationIntent);
                }
            break;
            case ALERT_RESPONSE:
                Log.d("mylog", "Notificación de tipo RESPONSE");
                notificationIntent = new Intent(this, SolicitudRecibida.class);
                for (Map.Entry<String, String> pair : remoteMessage.getData().entrySet()) {
                    notificationIntent.putExtra(pair.getKey(),pair.getValue());
                }
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(notificationIntent);
            break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence channelName = "Solicitudes de asistencia";
        String channelDescription = "Notificaciones de las solicitudes de asistencia de otros usuarios";
        NotificationChannel channel;
        channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(channelDescription);
        channel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void saveRegistrationToken(String Token){
        SharedPreferences preferences = getSharedPreferences("notificationToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", Token);
        editor.putBoolean("updated", false);
        editor.commit();
    }
}
