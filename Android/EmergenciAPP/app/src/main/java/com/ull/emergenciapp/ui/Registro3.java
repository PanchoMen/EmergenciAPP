package com.ull.emergenciapp.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.R;


public class Registro3 extends Fragment {

    private  View view;
    private  TextInputLayout alergias, otros, grupoSanguineoContainer;
    private  AutoCompleteTextView grupoSanguineo;
    private  ArrayAdapter<String> adapter;

    public Registro3() {
        // Required empty public constructor
    }

    public static Registro3 newInstance() {
        Registro3 fragment = new Registro3();
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
            view = inflater.inflate(R.layout.fragment_registro3, container, false);
            adapter = new ArrayAdapter<>(getContext(), R.layout.list_item, getResources().getStringArray(R.array.grupos_sanguineos));
            grupoSanguineo = view.findViewById(R.id.selectGrupoS_R3);
            grupoSanguineo.setAdapter(adapter);

            alergias = (TextInputLayout) view.findViewById(R.id.txtAlergias_R3);
            otros = (TextInputLayout) view.findViewById(R.id.txtOtrosDatos_R3);
            grupoSanguineoContainer = (TextInputLayout) view.findViewById(R.id.selectGrupoSContainer_R3);

            Bundle bundle = getArguments();
            if(bundle != null){
                grupoSanguineo.setText(bundle.getString("grupo_sanguineo", ""), false);
                alergias.getEditText().setText(bundle.getString("alergias", ""));
                otros.getEditText().setText(bundle.getString("otros", ""));
            }
        }
        return view;
    }

    public TextInputLayout getAlergias(){
        return alergias;
    }
    public TextInputLayout getOtrosDatos(){
        return otros;
    }
    public TextInputLayout getGrupoSanguineoContainer(){ return grupoSanguineoContainer; }
    public Integer getGrupoSanguineoSelected(){ return adapter.getPosition(grupoSanguineo.getText().toString()); }
    public AutoCompleteTextView getGrupoSanguineo(){ return grupoSanguineo; }
}
