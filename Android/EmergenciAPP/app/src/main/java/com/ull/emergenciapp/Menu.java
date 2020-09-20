package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Menu extends AppCompatActivity {
    private TextView nombre, email, tipoUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        nombre = (TextView) findViewById(R.id.txt_profileName_Me);
        String nombre_ = getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE).getString("nombre", "") + " " + getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE).getString("apellidos", "");
        nombre.setText(nombre_);

        email = (TextView) findViewById(R.id.txt_profileEmail_Me);
        email.setText(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getString("email", ""));

        tipoUser = (TextView) findViewById(R.id.txt_profileUserT_Me);
        tipoUser.setText(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getString("userTypeN", ""));

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(Menu.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    }
                });
    }

    public void closeMenu(View view) {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void goToPerfil(View view){
        Intent perfil = new Intent(this, Perfil.class);
        startActivity(perfil);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void goToHistorial(View view){
        Intent perfil = new Intent(this, Historial.class);
        startActivity(perfil);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void goToSettings(View view){
        Intent perfil = new Intent(this, Ajustes.class);
        startActivity(perfil);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void logout(View view) {
        SharedPreferences loginPreferences = getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = loginPreferences.edit();
        editor.putBoolean("logged", false);
        editor.commit();

        //Eliminar información del usuario
        getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE).edit().clear().commit();
        Intent register = new Intent(this, Login.class);
        register.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(register);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}
