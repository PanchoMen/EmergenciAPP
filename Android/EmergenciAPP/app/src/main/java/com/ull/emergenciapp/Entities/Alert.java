package com.ull.emergenciapp.Entities;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Alert {
    @SerializedName("id")
    private Integer id;

    @SerializedName("id_user")
    private Integer idUser;

    @SerializedName("id_sanitary")
    private Integer idSanitary;

    @SerializedName("user_latitude")
    private String userLatitude;

    @SerializedName("user_longitude")
    private String userLongitude;

    @SerializedName("sanitary_latitude")
    private String sanitaryLatitude;

    @SerializedName("sanitary_longitude")
    private String sanitaryLongitude;

    @SerializedName("accepted")
    private String accepted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Integer getIdSanitary() {
        return idSanitary;
    }

    public void setIdSanitary(Integer idSanitary) {
        this.idSanitary = idSanitary;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getSanitaryLatitude() {
        return sanitaryLatitude;
    }

    public void setSanitaryLatitude(String sanitaryLatitude) {
        this.sanitaryLatitude = sanitaryLatitude;
    }

    public String getSanitaryLongitude() {
        return sanitaryLongitude;
    }

    public void setSanitaryLongitude(String sanitaryLongitude) {
        this.sanitaryLongitude = sanitaryLongitude;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }
}
