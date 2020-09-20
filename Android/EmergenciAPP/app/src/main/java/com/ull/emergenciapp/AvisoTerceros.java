package com.ull.emergenciapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AvisoTerceros extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aviso_terceros);
    }

    public void enviar(View view){
        Intent send = new Intent(this, SolicitudEnviada.class);
        startActivity(send);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}