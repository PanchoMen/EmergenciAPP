package com.ull.emergenciapp.API;

import com.ull.emergenciapp.Entities.Alert;
import com.ull.emergenciapp.Entities.Response;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AlertService {
    String group = "alert/";
    @Multipart
    @POST(group + "sendAlert")
    Call<Response<Alert>> sendAlert(@Part("id") RequestBody id, @Part("latitude") RequestBody latitude, @Part("longitude") RequestBody longitude);

    @Multipart
    @POST(group + "acceptAlert")
    Call<Response<Alert>> acceptAlert(@Part("id_alert") RequestBody id_alert, @Part("id_sanitary") RequestBody id_sanitary, @Part("latitude") RequestBody latitude, @Part("longitude") RequestBody longitude, @Part("time") RequestBody time);

    @GET(group + "deleteAlert/{id}")
    Call<Response<String>> cancelAlert(@Path("id") String id);

    @GET(group + "historical/{id_user}")
    Call<Response<ArrayList<ArrayList<Alert>>>> getHistorical(@Path("id_user") String id_user);

    @GET(group + "check/{id}")
    Call<Response<Alert>> checkAlert(@Path("id") String id);

    @GET(group + "getEstimatedTime/{id}")
    Call<Response<String>> getEstimatedTime(@Path("id") String id);
}
