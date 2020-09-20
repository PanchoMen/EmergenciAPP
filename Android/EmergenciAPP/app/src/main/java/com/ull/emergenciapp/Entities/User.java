package com.ull.emergenciapp.Entities;

import com.google.gson.annotations.SerializedName;

import java.net.Inet4Address;

public class User {
    @SerializedName("id")
    private Integer id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("user_typeID")
    private Integer user_typeID;

    @SerializedName("user_type")
    private String user_type;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellidos")
    private String apellidos;

    @SerializedName("dni")
    private String dni;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("fecha_nacimiento")
    private String fecha_nacimiento;

    @SerializedName("sexoID")
    private Integer sexoID;

    @SerializedName("sexo")
    private String sexo;

    @SerializedName("grupo_sanguineoID")
    private Integer grupo_sanguineoID;

    @SerializedName("grupo_sanguineo")
    private String grupo_sanguineo;

    @SerializedName("alergias")
    private String alergias;

    @SerializedName("otros")
    private String otros;

    public Integer getId() { return id; }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setFecha_nacimiento(String fecha_nacimiento) { this.fecha_nacimiento = fecha_nacimiento; }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getGrupo_sanguineo() {
        return grupo_sanguineo;
    }

    public void setGrupo_sanguineo(String grupo_sanguineo) { this.grupo_sanguineo = grupo_sanguineo; }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getOtros() {
        return otros;
    }

    public void setOtros(String otros) {
        this.otros = otros;
    }

    public Integer getUser_typeID() { return user_typeID; }

    public void setUser_typeID(Integer user_typeID) { this.user_typeID = user_typeID; }

    public Integer getSexoID() { return sexoID; }

    public void setSexoID(Integer sexoID) { this.sexoID = sexoID; }

    public Integer getGrupo_sanguineoID() { return grupo_sanguineoID; }

    public void setGrupo_sanguineoID(Integer grupo_sanguineoID) { this.grupo_sanguineoID = grupo_sanguineoID; }
}
