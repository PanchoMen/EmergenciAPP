package com.ull.emergenciapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Login extends AppCompatActivity {
    TextInputLayout txtEmail, txtPassword;
    CheckBox checkSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = (TextInputLayout) findViewById(R.id.txtEmail_L);
        txtPassword = (TextInputLayout) findViewById(R.id.txtPassword_L);
        checkSesion = (CheckBox) findViewById(R.id.checkSesion);

        checkSesion();
    }

    public void goToRegister(View view){
        Intent register = new Intent(this, Registro.class);
        startActivity(register);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    private void checkSesion(){
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        if(preferences.getBoolean("sesion", false)){
            txtEmail.getEditText().setText(preferences.getString("email",""));
            txtPassword.getEditText().setText(preferences.getString("password",""));
            checkSesion.setChecked(true);
        }
    }

    private void goTo(){
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null) {
            switch (action) {
                case "Redirect":
                    if (intent.hasExtra("redirect")) {
                        if(!LocationService.isRunning){
                            Intent locationServiceIntent = new Intent(this, LocationService.class);
                            locationServiceIntent.setAction(LocationService.START_ACTION);
                            ContextCompat.startForegroundService(this, locationServiceIntent);
                        }
                        switch (intent.getStringExtra("redirect")) {
                            case "aceptarSolicitud":
                                Intent aceptarIntent = new Intent(Login.this, AceptarSolicitud.class);
                                aceptarIntent.putExtras(intent.getExtras());
                                redirectTo(aceptarIntent);
                                break;
                            case "SolicitudRecibida":
                                Intent recibidaIntent = new Intent(Login.this, SolicitudRecibida.class);
                                recibidaIntent.putExtras(intent.getExtras());
                                redirectTo(recibidaIntent);
                                break;
                            default:
                                goToMain();
                                break;
                        }
                    } else {
                        goToMain();
                    }
                    break;
                default:
                    goToMain();
                    break;
            }
        }else{
            goToMain();
        }
    }

    private void redirectTo(Intent intent){
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        finish();
    }

    private void goToMain(){
        Intent main = new Intent(this, MainActivity.class);
        redirectTo(main);
    }

    public void setSesion(User user){
        setSesionLogin(user);
        setSesionUserData(user);
    }

    private void setSesionLogin(User user){
        boolean save = checkSesion.isChecked();
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", user.getId());
        editor.putInt("userType", user.getUser_typeID());
        editor.putString("userTypeN", user.getUser_type());
        editor.putString("email", txtEmail.getEditText().getText().toString());
        if(save) {
            editor.putString("password", txtPassword.getEditText().getText().toString());
        }
        editor.putBoolean("sesion", save);
        editor.putBoolean("logged", true);
        editor.commit();
    }

    private void setSesionUserData(User user){
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nombre", user.getNombre());
        editor.putString("apellidos", user.getApellidos());
        editor.putString("dni", user.getDni());
        editor.putString("telefono", user.getTelefono());
        editor.putString("fecha_nacimiento", user.getFecha_nacimiento());
        editor.putString("sexo", user.getSexo());
        editor.putString("grupo_sanguineo", user.getGrupo_sanguineo());
        editor.putString("alergias", user.getAlergias());
        editor.putString("otros", user.getOtros());
        editor.commit();

    }

    public void login(View view) {
        String email = txtEmail.getEditText().getText().toString();
        String password = txtPassword.getEditText().getText().toString();

        if (email.isEmpty()) {
            txtEmail.setError("Campo vacío");
        }
        if (password.isEmpty()) {
            txtPassword.setError("Campo vacío");
        }
        if (!email.isEmpty() && !password.isEmpty()) {
            //Toast.makeText(Login.this, "Enviando...", Toast.LENGTH_SHORT).show();
            validarUser(email, password);
        }
    }

    private void validarUser(String email, String password) {
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<User>> call = userService.login(email, password);

        call.enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if(response.isSuccessful()){
                    Response<User> resp = response.body();
                    if(resp.getResult()){
                        User user = resp.getData();
                        setSesion(user);
                        updateToken(resp.getData().getId());
                        goTo();
                    }else{
                        getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("logged", resp.getResult());
                        Toast.makeText(Login.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Login.this, "Error en el proceso de login, inténtelo más tarde", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("logged", false);
                Toast.makeText(Login.this, "Error en el proceso de login, inténtelo más tarde", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void updateToken(int id){
        SharedPreferences notificationPreferences = getSharedPreferences(EmergenciAPP.NOTIFICATION_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        if(!notificationPreferences.getBoolean("updated", false) && notificationPreferences.getString("token", "") != "") {
            UserService userService = APIEngine.getAPIEngine().create(UserService.class);
            Call<Response<String>> call = userService.setToken(
                    RequestBody.create(String.valueOf(id), MediaType.parse("text/plain")),
                    RequestBody.create(notificationPreferences.getString("token", ""), MediaType.parse("text/plain"))
            );
            call.enqueue(new Callback<Response<String>>() {
                @Override
                public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                    if (response.isSuccessful()) {
                        Response<String> resp = response.body();
                        if (resp.getResult()) {
                            getSharedPreferences(EmergenciAPP.NOTIFICATION_TOKEN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("updated", true).commit();
                            Log.d("mylog", "Token de notificaciones actualizado correctamente");
                        } else {
                            getSharedPreferences(EmergenciAPP.NOTIFICATION_TOKEN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("updated", false).commit();
                            Log.d("mylog", "Fallo al actualizar el token de notificaciones");
                        }
                    }
                }

                @Override
                public void onFailure(Call<Response<String>> call, Throwable t) {
                    getSharedPreferences(EmergenciAPP.NOTIFICATION_TOKEN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("updated", false).commit();
                    Log.d("mylog", "Fallo al actualizar el token de notificaciones - ERROR: " + t.getMessage());
                }
            });
        }
    }
}