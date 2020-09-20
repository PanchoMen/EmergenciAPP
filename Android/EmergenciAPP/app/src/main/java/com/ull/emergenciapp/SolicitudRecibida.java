package com.ull.emergenciapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.AlertService;
import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class SolicitudRecibida extends AppCompatActivity {
    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private Thread dotsThread, timeThread;
    private TextView txt_Distancia;
    private String id_alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_recibida);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        txt_Distancia = (TextView) findViewById(R.id.txt_Distancia);
        mDotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);
        id_alert = getIntent().getStringExtra("id");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkTime();
        startDots();
    }

    private void checkTime(){
        int sleepTime = 1000 * 60 * 1;
        timeThread = new Thread() {
            public void run() {
                try {
                double time;
                do{
                    time = getEstimatedTime();
                    changeTime(time);
                    sleep(sleepTime);
                } while (time > 1.0);
                changeTime(time);
                timeThread.interrupt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timeThread.start();
    }

    private double getEstimatedTime() {
        AlertService alertService = APIEngine.getAPIEngine().create(AlertService.class);
        Call<Response<String>> call = alertService.getEstimatedTime(id_alert);
        try {
            return Double.valueOf(call.execute().body().getData());
        }catch (Exception e){
            Log.d("mylog", e.getMessage());
        }

        return 0.0;
    }

    private void changeTime(Double time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(time > 60.0){
                    int horas = (int) (time / 60.0);
                    int minutos = (int) (time % 60.0);
                    txt_Distancia.setText(horas + " h y " + minutos + " min");
                }else if(time > 1.0){
                    txt_Distancia.setText(round(time) + " min");
                }else{
                    txt_Distancia.setText("Menos de 1 min");
                }
            }
        });
    }

    private void startDots(){
        addDotsIndicator(2);
        dotsThread = new Thread() {
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

        dotsThread.start();
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
}