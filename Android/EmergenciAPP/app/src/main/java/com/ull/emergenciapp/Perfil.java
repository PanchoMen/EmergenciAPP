package com.ull.emergenciapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.ull.emergenciapp.ui.Registro2;
import com.ull.emergenciapp.ui.Registro3;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Perfil extends AppCompatActivity {

    private FrameLayout mSlideView;
    private LinearLayout mDotsLayout;
    private Button mBtn_next;
    private Button mBtn_prev;

    private TextView[] mDots;
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
        registro2 = new Registro2();
        registro2.setArguments(setData(0));
        registro2.setRetainInstance(true);
        registro3 = new Registro3();
        registro3.setArguments(setData(1));
        registro3.setRetainInstance(true);
        step = 0;

        addDotsIndicator(0);
        openFragment(-1, step);


        mBtn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkFields(step)) {
                    if (step < 1) {
                        openFragment(step, ++step);
                    } else if (step == 1) {
                        if(checkChanges()) {
                            updateData();
                        }else{
                            goToMenu(null);
                        }
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

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        goToMenu(null);
                    }
                });
    }

    public void openFragment(int current_, int new_){
        Fragment fragment = null;
        switch (new_){
            case 0:
                fragment = registro2;
                mBtn_prev.setVisibility(View.INVISIBLE);
                Drawable arrow_right = getResources().getDrawable( R.drawable.arrow_right );
                arrow_right.setBounds( 0, 0, arrow_right.getIntrinsicWidth(), arrow_right.getIntrinsicHeight());
                mBtn_next.setCompoundDrawables(null, null,null, arrow_right);
                break;
            case 1:
                mBtn_prev.setVisibility(View.VISIBLE);
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
        mDots = new TextView[2];
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

    private Bundle setData(int step){
        Bundle bundle = new Bundle();
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE);
        switch (step){
            case 0:
                bundle.putString("nombre", preferences.getString("nombre", ""));
                bundle.putString("apellidos", preferences.getString("apellidos", ""));
                bundle.putString("dni", preferences.getString("dni", ""));
                bundle.putString("telefono", preferences.getString("telefono", ""));
                bundle.putString("fecha_nacimiento", preferences.getString("fecha_nacimiento", ""));
                bundle.putString("sexo", preferences.getString("sexo", ""));
                break;
            case 1:
                bundle.putString("grupo_sanguineo", preferences.getString("grupo_sanguineo", ""));
                bundle.putString("alergias", preferences.getString("alergias", ""));
                bundle.putString("otros", preferences.getString("otros", ""));
                break;
        }
        return bundle;
    }

    private void updateData(){
        UserService userService = APIEngine.getAPIEngine().create(UserService.class);
        Call<Response<User>> call = userService.updateData(
                RequestBody.create(String.valueOf(getSharedPreferences(EmergenciAPP.LOGIN_PREFERENCES, Context.MODE_PRIVATE).getInt("id", -1)), MediaType.parse("text/plain")),
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
                        setSesionUserData(resp.getData());
                        Toast.makeText(Perfil.this, "Usuario actualizado correctamente", Toast.LENGTH_LONG).show();
                        goToMenu(null);
                    }else{
                        Toast.makeText(Perfil.this, resp.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Perfil.this, "Error en el proceso de actualización, inténtelo más tarde", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                Toast.makeText(Perfil.this, "Error en el proceso de actualización, inténtelo más tarde", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkFields(int step){
        boolean status = true;
        resetErrors(step);
        switch (step){
            case 0:
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
            case 1:
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
                registro2.getDNI().setError(null);
                registro2.getNombre().setError(null);
                registro2.getApellidos().setError(null);
                registro2.getTelefono().setError(null);
                registro2.getFechaNacimiento().setError(null);
                registro2.getSexoContainer().setError(null);
                break;
            case 1:
                registro3.getGrupoSanguineoContainer().setError(null);
                break;
        }
    }

    private boolean checkChanges(){
        SharedPreferences preferences = getSharedPreferences(EmergenciAPP.USER_DATA_PREFERENCES, Context.MODE_PRIVATE);
        if(!registro2.getNombre().getEditText().getText().toString().equals(preferences.getString("nombre", ""))){ return true; }
        if(!registro2.getApellidos().getEditText().getText().toString().equals(preferences.getString("apellidos", ""))){ return true; }
        if(!registro2.getDNI().getEditText().getText().toString().equals(preferences.getString("dni", ""))){ return true; }
        if(!registro2.getTelefono().getEditText().getText().toString().equals(preferences.getString("telefono", ""))){ return true; }
        if(!registro2.getFechaNacimiento().getEditText().getText().toString().equals(preferences.getString("fecha_nacimiento", ""))){ return true; }
        if(!registro2.getSexo().getText().toString().equals(preferences.getString("sexo", ""))){ return true; }
        if(!registro3.getGrupoSanguineo().getText().toString().equals(preferences.getString("grupo_sanguineo", ""))){ return true; }
        if(!registro3.getAlergias().getEditText().getText().toString().equals(preferences.getString("alergias", ""))){ return true; }
        if(!registro3.getOtrosDatos().getEditText().getText().toString().equals(preferences.getString("otros", ""))){ return true; }
        return false;
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

    public void goToMenu(View view){
        Intent intent = new Intent(Perfil.this, Menu.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}