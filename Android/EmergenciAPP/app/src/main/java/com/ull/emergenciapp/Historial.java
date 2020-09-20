package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class Historial extends AppCompatActivity {
    private ProgressBar loading;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        loading = (ProgressBar) findViewById(R.id.progressBar_H);
        layout = (LinearLayout) findViewById(R.id.layout_H);

        getHistorial();

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        goToMenu(null);
                    }
                });
    }

    private void getHistorial(){
        AlertService alertService = APIEngine.getAPIEngine().create(AlertService.class);
        Call<Response<ArrayList<ArrayList<Alert>>>> call = alertService.getHistorical(
                String.valueOf(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getInt("id", -1))
        );

        call.enqueue(new Callback<Response<ArrayList<ArrayList<Alert>>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<ArrayList<Alert>>>> call, retrofit2.Response<Response<ArrayList<ArrayList<Alert>>>> response) {
                if (response.isSuccessful()) {
                    Response<ArrayList<ArrayList<Alert>>> resp = response.body();
                    if (resp.getResult()) {
                        ArrayList<Alert> solicitudes = resp.getData().get(0);
                        ArrayList<Alert> asistencias = resp.getData().get(1);
                        for (Alert alert: solicitudes) {
                            addElement(alert, true);
                        }
                        for (Alert alert: asistencias) {
                            addElement(alert, false);
                        }
                        layout.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }
                }else{
                    loading.setVisibility(View.GONE);
                    Toast.makeText(Historial.this, "Error al recuperar la información", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Response<ArrayList<ArrayList<Alert>>>> call, Throwable t) {
                loading.setVisibility(View.GONE);
                Toast.makeText(Historial.this, "Error al recuperar la información", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addElement(Alert alert, boolean solicitud){
        String[] fecha = alert.getAccepted().split(" ")[0].split("-");
        String hora = alert.getAccepted().split(" ")[1];
        if(solicitud) {
            layout.addView(createElement("Ha recibido asistencia el día " + fecha[2] + "/" + fecha[1] + "/" + fecha[0] + " a las " + hora, R.color.colorPrimary));
        }else{
            layout.addView(createElement("Ha acudido a una solicitud de asistencia el día " + fecha[2] + "/" + fecha[1] + "/" + fecha[0] + " a las " + hora, R.color.colorAccent));
        }
    }

    public LinearLayout createElement(String texto, int idColor){
        TextView text = new TextView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(50,50,50,50);
        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(60);
        border.setColor(getResources().getColor(idColor));
        border.setStroke(5, getResources().getColor(R.color.white));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            container.setBackgroundDrawable(border);
        } else {
            container.setBackground(border);
        }

        text.setText(texto);
        text.setLines(3);
        text.setTextColor(getResources().getColor(R.color.white));
        container.addView(text);
        return container;
    }

    public void goToMenu(View view){
        Intent intent = new Intent(Historial.this, Menu.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}