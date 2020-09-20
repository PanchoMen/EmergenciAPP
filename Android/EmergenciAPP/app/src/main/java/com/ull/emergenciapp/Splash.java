package com.ull.emergenciapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;

import java.io.IOException;

import retrofit2.Call;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("mylog", "New Intent");
        if(intent.hasExtra("action")){
            switch (intent.getStringExtra("action")){
                case "Alert":
                    Log.d("mylog", "Se ha pulsado una notificación de ayuda");
                    Intent aceptarIntent = new Intent(Splash.this, AceptarSolicitud.class);
                    aceptarIntent.putExtras(intent.getExtras());
                    Intent failIntent1 = new Intent(Splash.this, Login.class);
                    failIntent1.setAction("Redirect");
                    failIntent1.putExtra("redirect", "aceptarSolicitud");
                    failIntent1.putExtras(intent.getExtras());
                    checkSesion(aceptarIntent, failIntent1);
                    break;
                case "Response":
                    Log.d("mylog", "Se ha recibido una notificación de aceptación de alerta");
                    Intent solicitudRecibidaIntent = new Intent(Splash.this, SolicitudRecibida.class);
                    solicitudRecibidaIntent.putExtras(intent.getExtras());

                    Intent failIntent2 = new Intent(Splash.this, Login.class);
                    failIntent2.setAction("Redirect");
                    failIntent2.putExtra("redirect", "SolicitudRecibida");
                    failIntent2.putExtras(intent.getExtras());
                    checkSesion(solicitudRecibidaIntent, failIntent2);
                    break;
                default:
                    defaultAction();
                    break;
            }
        }else{
            Log.d("mylog", "MAIN Intent");
            defaultAction();
        }
    }

    private void defaultAction(){
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("mylog", "VERSION 23 o MAYOR");
            checkPermission();
        } else {
            Intent successIntent = new Intent(Splash.this, MainActivity.class);
            Intent failIntent = new Intent(Splash.this, Login.class);
            failIntent.setAction("Main");
            checkSesion(successIntent, failIntent);
        }
    }

    private void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(
                Splash.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.d("mylog", "Permisos de localizacion NO concedidos");
            ActivityCompat
                    .requestPermissions(
                            Splash.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
        }
        else {
            //startLocationService();
            Log.d("mylog", "Permisos de localizacion YA concedidos");
            Intent successIntent = new Intent(Splash.this, MainActivity.class);
            Intent failIntent = new Intent(Splash.this, Login.class);
            failIntent.setAction("Main");
            checkSesion(successIntent, failIntent);
        }
    }

    public void checkSesion(Intent successIntent, Intent failIntent){
        new doLogin().execute(successIntent, failIntent);
    }

    private boolean validarUser(String email, String password) {
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<User>> call = userService.login(email, password);
        try {
            Response<User> response = call.execute().body();
            if(response.getResult()) {
                setSesion(response.getData());
            }
            //getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("logged", response.getResult());
            return response.getResult();
        } catch (IOException e) {
            getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("logged", false);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("mylog", "Permision Request: Permisos de localizacion concedidos");
                    Intent successIntent = new Intent(Splash.this, MainActivity.class);
                    Intent failIntent = new Intent(Splash.this, Login.class);
                    failIntent.setAction("Main");
                    checkSesion(successIntent, failIntent);
                }else{
                    Log.d("mylog", "Permision Request: Permisos de localizacion NO concedidos");
                    Toast.makeText(Splash.this, "Los permisos de ubicación son esenciales para el funcionamiento de la aplicación", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Splash.this.finishAndRemoveTask();
                }
                break;
        }
    }

    class doLogin extends AsyncTask<Intent, Object, Boolean>{
        Intent successIntent;
        Intent failIntent;
        @Override
        protected Boolean doInBackground(Intent[] params) {
            successIntent = params[0];
            failIntent = params[1];
            SharedPreferences preferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
            if(preferences.getBoolean("logged", false)){
                    return validarUser(preferences.getString("email", ""), preferences.getString("password", ""));
            }
            /*if(preferences.getBoolean("sesion", false)){
                return validarUser(preferences.getString("email", ""), preferences.getString("password", ""));
            }*/
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //super.onPostExecute(o);
            Intent intent = failIntent;
            if(result){
                intent = successIntent;
            }
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            Splash.this.finish();
        }
    }

    public void setSesion(User user){
        setSesionLogin(user);
        setSesionUserData(user);
    }

    private void setSesionLogin(User user){
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", user.getId());
        editor.putInt("userType", user.getUser_typeID());
        editor.putString("userTypeN", user.getUser_type());
        editor.putString("email", user.getEmail());
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
}
