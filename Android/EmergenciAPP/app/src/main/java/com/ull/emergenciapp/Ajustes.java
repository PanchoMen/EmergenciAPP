package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Ajustes extends AppCompatActivity {

    private TextInputLayout radioCont, frecuenciaCont;
    private TextInputEditText radio, frecuencia;
    private Button btnGuardar;
    private ProgressBar loading;
    private boolean sanitario;
    private String oldRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        radioCont = (TextInputLayout) findViewById(R.id.txtRadio_Aj);
        frecuenciaCont = (TextInputLayout) findViewById(R.id.txtFrecuencia_Aj);
        radio = (TextInputEditText) radioCont.getEditText();
        frecuencia = (TextInputEditText) frecuenciaCont.getEditText();
        btnGuardar = (Button) findViewById(R.id.btn_guardar_Aj);
        loading = (ProgressBar) findViewById(R.id.progressBar_Aj);

        SharedPreferences loginPreferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        sanitario = (loginPreferences.getInt("userType", -1) != EmergenciAPP.USUARIO_GENERAL);

        showData();

        if(sanitario) {
            radio.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    radioCont.setHelperText("Distancia en metros en la cual se desean recibir notificaciones");
                } else {
                    radioCont.setHelperText(null);
                    if (radioCont.getEditText().getText().toString().length() > 6) {
                        radioCont.setError("Máximo 6 dígitos");
                    } else {
                        radioCont.setError(null);
                    }
                }
            });
        }

        frecuencia.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                frecuenciaCont.setHelperText("Frecuencia en minutos con la que se actualiza la ubicación");
            } else {
                frecuenciaCont.setHelperText(null);
            }
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        goToMenu(null);
                    }
                });
    }

    public void goToMenu(View view){
        Intent intent = new Intent(Ajustes.this, Menu.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void showData(){
        frecuencia.setText(String.valueOf(EmergenciAPP.UPDATE_INTERVAL));
        if(sanitario){
            getRadius();
        }else{
            showElements();
        }
    }

    public void saveData(View view){
        if(checkData()) {
            saveFrecuency();
            if (sanitario) {
                saveRadius();
            }
        }else{
            goToMenu(null);
        }
    }

    private void saveRadius(){
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<String>> call = userService.setRadius(
                RequestBody.create(String.valueOf(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getInt("id", -1)), MediaType.parse("text/plain")),
                RequestBody.create(radio.getText().toString(), MediaType.parse("text/plain"))
        );

        call.enqueue(new Callback<Response<String>>() {
            @Override
            public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                if (response.isSuccessful()) {
                    Response<String> resp = response.body();
                    if (resp.getResult()) {
                        Toast.makeText(Ajustes.this, "Usuario actualizado correctamente", Toast.LENGTH_LONG).show();
                        goToMenu(null);
                    }else{
                        Toast.makeText(Ajustes.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Ajustes.this, "Error en el proceso de actualización, inténtelo más tarde", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Response<String>> call, Throwable t) {

            }
        });
    }

    private void getRadius(){
            UserService userService = APIEngine.getAPIEngine().create(UserService.class);
            Call<Response<String>> call = userService.getRadius(
                String.valueOf(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getInt("id", -1))
        );

            call.enqueue(new Callback<Response<String>>() {
                @Override
                public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                    if (response.isSuccessful()) {
                        Response<String> resp = response.body();
                        if (resp.getResult()) {
                            oldRadius = resp.getData();
                            radio.setText(resp.getData());
                            showElements();
                        }else{
                            Toast.makeText(Ajustes.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(Ajustes.this, "Error al obtener el radio, inténtelo más tarde", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<String>> call, Throwable t) {
                    Toast.makeText(Ajustes.this, "Error al obtener el radio, inténtelo más tarde", Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveFrecuency(){
        EmergenciAPP.UPDATE_INTERVAL = Long.valueOf(frecuencia.getText().toString());
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
        if(!sanitario){
            Toast.makeText(Ajustes.this, "Usuario actualizado correctamente", Toast.LENGTH_LONG).show();
            goToMenu(null);
        }
    }

    private void showElements(){
        frecuenciaCont.setVisibility(View.VISIBLE);
        if(sanitario){
            radioCont.setVisibility(View.VISIBLE);
        }
        btnGuardar.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private boolean checkData(){
        if(sanitario) {
            if (!radio.getText().toString().equals(oldRadius)) { return true; }
        }
        if (!frecuencia.getText().toString().equals(EmergenciAPP.UPDATE_INTERVAL)) { return true; }
        Log.d("mylog", "Los ajustes no necesitan ser actualizados");
        return false;
    }

}