package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.R;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SolicitudEnviada extends AppCompatActivity {

    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private Thread t;
    private View mView;
    private Dialog dialog;
    private String id_alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_enviada);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mDotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        Intent intent = getIntent();
        id_alert = intent.getStringExtra("id_alert");

        mView = getLayoutInflater().inflate(R.layout.dialog_cancel_white, null);
        dialog = new MaterialAlertDialogBuilder(this)
                .setView(mView)
                .create();

        start();

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        cancel(null);
                    }
                });
    }

    private void start() {
        addDotsIndicator(2);
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

        t.start();
    }

    public void cancel(View view){
        dialog.show();
    }

    public void cancelarDialog(View view) {
        dialog.dismiss();
    }

    public void cancelarSolicitud(View view){

        AlertService alertService = APIEngine.getAPIEngine().create(AlertService.class);
        Call<Response<String>> call = alertService.cancelAlert(
                id_alert
        );

        dialog.dismiss();
        call.enqueue(new Callback<Response<String>>() {
            @Override
            public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                if(response.isSuccessful()) {
                    Response<String> resp = response.body();
                    if (resp.getResult()) {
                        if (t.isAlive()) {
                            t.interrupt();
                        }
                        Intent main = new Intent(SolicitudEnviada.this, MainActivity.class);
                        startActivity(main);
                        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                        SolicitudEnviada.this.finish();
                    }else{
                        Toast.makeText(SolicitudEnviada.this, "No se ha podido cancelar la solicitud", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Response<String>> call, Throwable t) {
                Toast.makeText(SolicitudEnviada.this, "No se ha podido cancelar la solicitud", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addDotsIndicator(int position){
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
}