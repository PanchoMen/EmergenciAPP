package com.ull.emergenciapp.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;
import com.ull.emergenciapp.R;
import com.ull.emergenciapp.Registro;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Registro2 extends Fragment {

    private  View view;
    private  TextInputLayout nombre, apellidos, dni, telefono, fechaNacimiento, sexoContainer;
    private  AutoCompleteTextView sexo;
    private  ArrayAdapter<String> adapter;
    private  MaterialDatePicker datePicker;
    private Calendar calendar;


    public Registro2() {
        // Required empty public constructor
    }

    public static Registro2 newInstance() {
        Registro2 fragment = new Registro2();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_registro2, container, false);
            adapter = new ArrayAdapter<>(getContext(), R.layout.list_item, getResources().getStringArray(R.array.tipos_sexo));
            sexo = view.findViewById(R.id.selectSexo_R2);
            sexo.setAdapter(adapter);

            nombre = (TextInputLayout) view.findViewById(R.id.txtNombre_R2);
            apellidos = (TextInputLayout) view.findViewById(R.id.txtApellidos_R2);
            dni = (TextInputLayout) view.findViewById(R.id.txtDNI_R2);
            telefono = (TextInputLayout) view.findViewById(R.id.txtTelefono_R2);
            fechaNacimiento = (TextInputLayout) view.findViewById(R.id.selectFechaN_R2);
            sexoContainer = (TextInputLayout) view.findViewById(R.id.selectSexoContainer_R2);

            MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
            CalendarConstraints.Builder CCBuilder = new CalendarConstraints.Builder();
            builder.setTitleText("Fecha de nacimiento");
            datePicker = builder.build();

            fechaNacimiento.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
                }
            });

            datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                @Override
                public void onPositiveButtonClick(Object selection) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String formatedDate = sdf.format(datePicker.getSelection());
                    fechaNacimiento.getEditText().setText(formatedDate);
                    //fechaNacimiento.getEditText().setText(datePicker.getHeaderText());
                }
            });

            Bundle bundle = getArguments();
            if(bundle != null){
                nombre.getEditText().setText(bundle.getString("nombre", ""));
                apellidos.getEditText().setText(bundle.getString("apellidos", ""));
                dni.getEditText().setText(bundle.getString("dni", ""));
                telefono.getEditText().setText(bundle.getString("telefono", ""));
                fechaNacimiento.getEditText().setText(bundle.getString("fecha_nacimiento", ""));
                sexo.setText(bundle.getString("sexo", ""), false);
            }
        }
        return view;
    }



    public TextInputLayout getNombre(){ return nombre; }
    public TextInputLayout getApellidos(){ return apellidos; }
    public TextInputLayout getDNI(){ return dni; }
    public TextInputLayout getTelefono(){ return telefono; }
    public TextInputLayout getFechaNacimiento(){ return fechaNacimiento; }
    public TextInputLayout getSexoContainer(){ return  sexoContainer; }
    public AutoCompleteTextView  getSexo(){ return sexo; }
    public Integer getSexoSelected(){ return adapter.getPosition(sexo.getText().toString()); }
}
