package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private int time = 10;
    private Dialog dialog;
    private View mView;
    private TextView mensajeDialog;
    private Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = getLayoutInflater().inflate(R.layout.dialog_alert_red, null);
        mensajeDialog = (TextView) mView.findViewById(R.id.text_mensaje);
        dialog = new MaterialAlertDialogBuilder(this)
                .setView(mView)
                .create();

        if(!LocationService.isRunning){
            Intent locationServiceIntent = new Intent(this, LocationService.class);
            locationServiceIntent.setAction(LocationService.START_ACTION);
            ContextCompat.startForegroundService(this, locationServiceIntent);
        }

        t = new Thread(){
            public void run() {
                for (int i = time; i > 0; i--) {
                    try {
                        final int a = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensajeDialog.setText("Se enviará una solicitud de asistencia para usted en " + a + " segundos");
                            }
                        });
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(dialog.isShowing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                solicitudEnviada(findViewById(android.R.id.content));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            };
                        }
                    });
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    public void openMenu(View view) {
        Intent register = new Intent(this, Menu.class);
        startActivity(register);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public void newAlert(View view) {
        if(t.isAlive()){
            t.interrupt();
        }
        dialog.show();
        t.start();
    }

    public void avisoTerceros(View view) throws InterruptedException {
        dialog.dismiss();
        t.interrupt();
        /*Intent avisoTercero = new Intent(this, AvisoTerceros.class);
        startActivity(avisoTercero);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);*/
    }

    public void solicitudEnviada(View view) throws InterruptedException {
        sendAlert();
        dialog.dismiss();
        t.interrupt();
    }

    public void sendAlert(){
        SharedPreferences loginPreferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Location lastLocation = LocationService.getLastLocation();
        AlertService alertService = APIEngine.getAPIEngine().create(AlertService.class);
        Call<Response<Alert>> call = alertService.sendAlert(
                RequestBody.create(String.valueOf(loginPreferences.getInt("id", -1)), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(lastLocation.getLatitude()), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(lastLocation.getLongitude()), MediaType.parse("text/plain"))
        );
        call.enqueue(new Callback<Response<Alert>>() {
            @Override
            public void onResponse(Call<Response<Alert>> call, retrofit2.Response<Response<Alert>> response) {
                if(response.isSuccessful()){
                    Response<Alert> resp = response.body();
                    if(resp.getResult()){
                        Log.d("mylog", "Solicitud de asistencia enviada correctamente");
                        Intent solicitudEnviada = new Intent(MainActivity.this, SolicitudEnviada.class);
                        /*Bundle bundle = new Bundle();
                        bundle.putString("id_alert", String.valueOf(resp.getData().getId()));
                        solicitudEnviada.putExtras(bundle);
                         */
                        solicitudEnviada.putExtra("id_alert", String.valueOf(resp.getData().getId()));
                        startActivity(solicitudEnviada);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    }else{
                        Log.d("mylog", resp.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<Response<Alert>> call, Throwable t) {
                Log.d("mylog", t.getMessage());
            }
        });
    }
}
