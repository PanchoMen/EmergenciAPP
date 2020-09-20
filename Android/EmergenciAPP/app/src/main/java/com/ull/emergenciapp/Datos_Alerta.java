package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Datos_Alerta extends AppCompatActivity {

    private TextView grupoS, alergias, otros;
    private TextInputLayout cont_grupoS, cont_alergias, cont_otros;
    private Button llamar;
    private ProgressBar loading;
    private String numeroTf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_alerta);
        loading = (ProgressBar) findViewById(R.id.progressBar);
        cont_grupoS = (TextInputLayout) findViewById(R.id.txt_cont_GSanguineo_DA);
        cont_alergias = (TextInputLayout) findViewById(R.id.txt_cont_alergias_DA);
        cont_otros = (TextInputLayout) findViewById(R.id.txt_cont_otros_DA);
        grupoS = (TextView) findViewById(R.id.txt_GSanguineo_DA);
        alergias = (TextView) findViewById(R.id.txt_alergias_DA);
        otros = (TextView) findViewById(R.id.txt_otros_DA);
        llamar = (Button) findViewById(R.id.btn_llamar);

        getData(getIntent().getStringExtra("id_user"));

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        goToMain(null);
                    }
                });
    }

    private void getData(String user_id){
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<User>> call = userService.getData(
            user_id
        );

        call.enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if (response.isSuccessful()) {
                    Response<User> resp = response.body();
                    if(resp.getResult()) {
                        User userData = resp.getData();
                        loading.setVisibility(View.GONE);
                        String grupoS_, alergias_, otros_;
                        grupoS_ = userData.getGrupo_sanguineo();
                        alergias_ = userData.getAlergias();
                        otros_ = userData.getOtros();
                        if(grupoS_.isEmpty()){ grupoS_ = "Sin datos que mostrar";}
                        if(alergias_.isEmpty()){ alergias_ = "Sin datos que mostrar";}
                        if(otros_.isEmpty()){ otros_ = "Sin datos que mostrar";}
                        grupoS.setText(grupoS_);
                        alergias.setText(alergias_);
                        otros.setText(otros_);
                        numeroTf = userData.getTelefono();
                        cont_grupoS.setVisibility(View.VISIBLE);
                        cont_alergias.setVisibility(View.VISIBLE);
                        cont_otros.setVisibility(View.VISIBLE);
                        if(!numeroTf.isEmpty()) {
                            llamar.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    loading.setVisibility(View.GONE);
                    Toast.makeText(Datos_Alerta.this, "Error al recuperar la información", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                loading.setVisibility(View.GONE);
                Toast.makeText(Datos_Alerta.this, "Error al recuperar la información", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void goToMain(View view){
        Intent intent = new Intent(Datos_Alerta.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public void llamar(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numeroTf));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }else{
            Toast.makeText(Datos_Alerta.this, "No es posible realizar esta acción", Toast.LENGTH_LONG).show();
        }
    }
}