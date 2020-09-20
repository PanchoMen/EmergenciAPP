package com.ull.emergenciapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ull.emergenciapp.API.APIEngine;
import com.ull.emergenciapp.API.UserService;
import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;
import com.ull.emergenciapp.ui.Registro1;
import com.ull.emergenciapp.ui.Registro2;
import com.ull.emergenciapp.ui.Registro3;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;


public class Registro extends AppCompatActivity {
    private FrameLayout mSlideView;
    private LinearLayout mDotsLayout;
    private Button mBtn_next;
    private Button mBtn_prev;

    private TextView[] mDots;
    private Registro1 registro1;
    private Registro2 registro2;
    private Registro3 registro3;

    private Integer step;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mSlideView = (FrameLayout) findViewById(R.id.slideView);
        mDotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);
        mBtn_next = (Button) findViewById(R.id.btn_next);
        mBtn_prev = (Button) findViewById(R.id.btn_prev);
        registro1 = new Registro1();
        registro1.setRetainInstance(true);
        registro2 = new Registro2();
        registro2.setRetainInstance(true);
        registro3 = new Registro3();
        registro3.setRetainInstance(true);
        step = 0;

        addDotsIndicator(0);
        openFragment(-1, step);


        mBtn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkFields(step)) {
                    if (step < 2) {
                        openFragment(step, ++step);
                    } else if (step == 2) {
                        register();
                        Toast.makeText(getApplicationContext(), "ENVIANDO...", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mBtn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkFields(step)) {
                    if (step > 0) {
                        openFragment(step, --step);
                    }
                }
            }
        });
    }

    public void openFragment(int current_, int new_){
        Fragment fragment = null;
        switch (new_){
            case 0:
                fragment = registro1;
                mBtn_prev.setVisibility(View.INVISIBLE);
                break;
            case 1:
                Drawable arrow_right = getResources().getDrawable( R.drawable.arrow_right );
                arrow_right.setBounds( 0, 0, arrow_right.getIntrinsicWidth(), arrow_right.getIntrinsicHeight());

                mBtn_prev.setVisibility(View.VISIBLE);
                mBtn_next.setCompoundDrawables(null, null,null,arrow_right);
                fragment = registro2;
                break;
            case 2:
                Drawable done = getResources().getDrawable( R.drawable.done );
                done.setBounds( 0, 0, done.getIntrinsicWidth(), done.getIntrinsicHeight());
                mBtn_next.setCompoundDrawables(null, null,null, done);
                fragment = registro3;
                break;
        }
        addDotsIndicator(new_);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(current_ < new_) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_right);
        }else{
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_left, R.anim.exit_to_left);
        }
        String tag = "Registro" + new_;
        transaction.replace(R.id.slideView, fragment, tag).commit();
    }

    public void addDotsIndicator(int position){
        mDots = new TextView[3];
        mDotsLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.white));

            mDotsLayout.addView(mDots[i]);
        }
        if(mDots.length > 0){
            mDots[position].setTextColor(getResources().getColor(R.color.white));
            mDots[position].setTextSize(40);
        }
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public void goToLogin(){
        Intent login = new Intent(this, Login.class);
        startActivity(login);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    private void register(){
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<User>> call = userService.register(
                RequestBody.create(registro1.getEmail1().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro1.getPassword1().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(registro1 .getTipoUserSelected() + 1), MediaType.parse("text/plain")),
                RequestBody.create(registro2.getNombre().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro2.getApellidos().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro2.getDNI().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro2.getTelefono().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro2.getFechaNacimiento().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(registro2.getSexoSelected() + 1), MediaType.parse("text/plain")),
                RequestBody.create(String.valueOf(registro3.getGrupoSanguineoSelected() + 1), MediaType.parse("text/plain")),
                RequestBody.create(registro3.getAlergias().getEditText().getText().toString(), MediaType.parse("text/plain")),
                RequestBody.create(registro3.getOtrosDatos().getEditText().getText().toString(), MediaType.parse("text/plain"))
                );

        call.enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if(response.isSuccessful()){
                    Response<User> resp = response.body();
                    if(resp.getResult()){
                        Toast.makeText(Registro.this, "Usuario creado correctamente", Toast.LENGTH_LONG).show();
                        goToLogin();
                    }else{
                        Toast.makeText(Registro.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Registro.this, "Error en el proceso de registro, inténtelo más tarde", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                Toast.makeText(Registro.this, "Error en el proceso de registro, inténtelo más tarde", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkFields(int step){
        boolean status = true;
        resetErrors(step);
        switch (step){
            case 0:
                if(!checkEmail()){
                    status = false;
                }
                if(!checkPassword()){
                    status = false;
                }
                if(registro1.getTipoUserSelected() == -1){
                    registro1.getTipoUserContainer().setError("Seleccione una opción");
                    status = false;
                }
            break;
            case 1:
                if(registro2.getNombre().getEditText().getText().toString().isEmpty()){
                    registro2.getNombre().setError("Campo vacío");
                    status = false;
                }
                if(registro2.getApellidos().getEditText().getText().toString().isEmpty()){
                    registro2.getApellidos().setError("Campo vacío");
                    status = false;
                }
                if(registro2.getDNI().getEditText().getText().toString().isEmpty()){
                    registro2.getDNI().setError("Campo vacío");
                    status = false;
                }
                if(registro2.getTelefono().getEditText().getText().toString().isEmpty()){
                    registro2.getTelefono().setError("Campo vacío");
                    status = false;
                }
                if(registro2.getFechaNacimiento().getEditText().getText().toString().isEmpty()){
                    registro2.getFechaNacimiento().setError("Campo vacío");
                    status = false;
                }
                if(registro2.getSexoSelected() == -1){
                    registro2.getSexoContainer().setError("Seleccione una opción");
                    status = false;
                }
            break;
            case 2:
                if(registro3.getGrupoSanguineoSelected() == -1){
                    registro3.getGrupoSanguineoContainer().setError("Seleccione una opción");
                    status = false;
                }
            break;
        }
        return status;
    }

    private void resetErrors(int step){
        switch (step) {
            case 0:
                registro1.getEmail1().setError(null);
                registro1.getEmail2().setError(null);
                registro1.getPassword1().setError(null);
                registro1.getPassword2().setError(null);
                registro1.getTipoUserContainer().setError(null);
                break;

            case 1:
                registro2.getDNI().setError(null);
                registro2.getNombre().setError(null);
                registro2.getApellidos().setError(null);
                registro2.getTelefono().setError(null);
                registro2.getFechaNacimiento().setError(null);
                registro2.getSexoContainer().setError(null);
                break;
            case 2:
                registro3.getGrupoSanguineoContainer().setError(null);
                break;
        }
    }

    private boolean checkEmail(){
        boolean status = true;
        String email1 = registro1.getEmail1().getEditText().getText().toString();
        String email2 = registro1.getEmail2().getEditText().getText().toString();
        if(email1.isEmpty()) {
            if (registro1.getEmail1().getEditText().getText().toString().isEmpty()) {
                registro1.getEmail1().setError("Campo vacío");
            }
            status = false;
        }
        if(email2.isEmpty()){
            if(registro1.getEmail2().getEditText().getText().toString().isEmpty()){
                registro1.getEmail2().setError("Campo vacío");
            }
            status = false;
        }
        if(status) {
            if(!email1.equals(email2)) {
                registro1.getEmail1().setError("Los emails no coinciden");
                registro1.getEmail2().setError("Los emails no coinciden");
                status = false;
            }
        }
        return status;
    }

    private boolean checkPassword(){
        boolean status = true;
        String pass1 = registro1.getPassword1().getEditText().getText().toString();
        String pass2 = registro1.getPassword2().getEditText().getText().toString();
        if(pass1.isEmpty()){
            if(registro1.getPassword1().getEditText().getText().toString().isEmpty()){
                registro1.getPassword1().setError("Campo vacío");
            }
            status = false;
        }
        if(pass2.isEmpty()){
            if(registro1.getPassword2().getEditText().getText().toString().isEmpty()){
                registro1.getPassword2().setError("Campo vacío");
            }
            status = false;
        }
        if(status){
            if(!pass1.equals(pass2)) {
                registro1.getPassword1().setError("Las contraseñas no coinciden");
                registro1.getPassword2().setError("Las contraseñas no coinciden");
                status = false;
            }
        }
        return status;
    }
}
