package com.ull.emergenciapp.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.R;

import java.lang.reflect.Array;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Registro1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Registro1 extends Fragment {

    private  View view;
    private TextInputLayout email1, email2, password1, password2, tipoUserContainer;
    private AutoCompleteTextView  tipoUser;
    private ArrayAdapter<String> adapter;

    public Registro1() {
        // Required empty public constructor
    }

    public static Registro1 newInstance() {
        Registro1 fragment = new Registro1();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_registro1, container, false);
            adapter = new ArrayAdapter<>(getContext(), R.layout.list_item, getResources().getStringArray(R.array.tipos_usuarios));
            tipoUser = view.findViewById(R.id.selectTiposUsuarios_R);
            tipoUser.setAdapter(adapter);

            email1 = (TextInputLayout) view.findViewById(R.id.txtEmail_R_1);
            email2 = (TextInputLayout) view.findViewById(R.id.txtEmail_R_2);
            password1 = (TextInputLayout) view.findViewById(R.id.txtPassword_R_1);
            password2 = (TextInputLayout) view.findViewById(R.id.txtPassword_R_2);
            tipoUserContainer = (TextInputLayout) view.findViewById(R.id.selectTiposUsuariosContainer_R);
        }
        return view;
    }

    public TextInputLayout getEmail1(){
        return email1;
    }
    public TextInputLayout getEmail2(){
        return email2;
    }
    public TextInputLayout getPassword1(){
        return password1;
    }
    public TextInputLayout getPassword2(){
        return password2;
    }
    public TextInputLayout  getTipoUserContainer(){ return tipoUserContainer; }
    public AutoCompleteTextView  getTipoUser(){ return tipoUser; }
    public int getTipoUserSelected(){ return adapter.getPosition(tipoUser.getText().toString()); }
}

