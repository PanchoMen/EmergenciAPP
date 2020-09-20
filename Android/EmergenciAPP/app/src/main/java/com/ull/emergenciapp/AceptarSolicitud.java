package com.ull.emergenciapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AceptarSolicitud extends AppCompatActivity{
    private TextView distancia;
    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private Thread t;
    private String id_alert;
    private Double distance;
    public static Location alertLocation;
    private String id_user;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceptar_solicitud);

        if(!LocationService.isRunning){
            Intent locationServiceIntent = new Intent(this, LocationService.class);
            locationServiceIntent.setAction(LocationService.START_ACTION);
            ContextCompat.startForegroundService(this, locationServiceIntent);
        }

        distancia = (TextView) findViewById(R.id.txt_Distancia);
        mDotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        Intent intent = getIntent();
        id_alert = intent.getStringExtra("id_alert");
        id_user = intent.getStringExtra("id_user");

        String dist = "..."  + " KM";
        if(intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            alertLocation = new Location("");
            alertLocation.setLatitude(Double.valueOf(intent.getStringExtra("latitude")));
            alertLocation.setLongitude(Double.valueOf(intent.getStringExtra("longitude")));

            Location myLocation = LocationService.getLastLocation();

            distance = Math.round((alertLocation.distanceTo(myLocation)/1000) * 100.0) / 100.0;
            dist = String.valueOf(distance) + " KM";
        }
        distancia.setText(dist);

        initDots();
    }

    public void acceptAlert(View view){
        sendAccepted();
        Log.d("mylog", "SE HA ACEPTADO LA SOLICITUD");
    }

    public void reclineAlert(View view){
        Log.d("mylog", "SE HA RECHAZADO LA SOLICITUD");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        AceptarSolicitud.this.finishAndRemoveTask();
        t.interrupt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mylog", "Has vuelto de google maps");
        Intent datosAlerta = new Intent(this, Datos_Alerta.class);
        datosAlerta.putExtra("id_alert", id_alert);
        datosAlerta.putExtra("id_user", id_user);
        datosAlerta.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(datosAlerta);
    }

    private void sendAccepted() {

        SharedPreferences userPreferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Location lastLocation = LocationService.getLastLocation();
        AlertService alertService = APIEngine.getAPIEngine().create(AlertService.class);
        Call<Response<Alert>> call = alertService.acceptAlert(
                RequestBody.create(id_alert, MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(userPreferences.getInt("id", -1)), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(lastLocation.getLatitude()), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(lastLocation.getLongitude()), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(lastLocation.getLongitude()), MediaType.parse("text/plain"))
        );

        call.enqueue(new Callback<Response<Alert>>() {
            @Override
            public void onResponse(Call<Response<Alert>> call, retrofit2.Response<Response<Alert>> response) {
                if (response.isSuccessful()) {
                    Response<Alert> resp = response.body();
                    if(resp.getResult()){
                        Log.d("mylog", "Solicitud aceptada correctamente");
                        t.interrupt();
                        String url = "https://www.google.com/maps/dir/?api=1&destination=" + alertLocation.getLatitude() + "," + alertLocation.getLongitude();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivityForResult(intent, 0);
                    }else{
                        Toast.makeText(AceptarSolicitud.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                        reclineAlert(null);
                    }
                }else{
                    Toast.makeText(AceptarSolicitud.this, "Error al aceptar la solicitud, inténtelo de nuevo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Alert>> call, Throwable t) {
                Log.d("mylog", "Error al aceptar la solicitud - ERROR: " + t.getMessage());
            }
        });

    }

    private String getStimatedTime(){
        return String.valueOf((distance / 5.3) * 60);
    }

    private void addDotsIndicator(int position){
        mDots = new TextView[5];
        mDotsLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(65);
            mDots[i].setTextColor(getResources().getColor(R.color.white));
            mDots[i].setAlpha(0);

            mDotsLayout.addView(mDots[i]);
        }
        if(mDots.length > 0){
            setTransparence(position, 1);
            setTransparence(position - 1, (float)0.5);
            setTransparence(position - 2, (float)0.25);
        }
    }

    private void setTransparence(int position, float transparence){
        if(position < mDots.length && position >= 0){
            mDots[position].setAlpha(transparence);
        }
    }

    private void initDots(){
        t = new Thread() {
            public void run () {
                while(true) {
                    for (int i = 0; i < (mDots.length + 2); i++) {
                        try {
                            final int a = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addDotsIndicator(a);
                                }
                            });
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        addDotsIndicator(2);
        t.start();
    }
}