package com.ull.emergenciapp.API;

import com.ull.emergenciapp.Entities.Response;
import com.ull.emergenciapp.Entities.User;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface UserService {
    String group = "users/";
    String subgroup = "notificationPreferences/";

    @GET(group + "login/{email}/{password}")
    Call<Response<User>> login(@Path("email") String email, @Path("password") String password);

    @Multipart
    @POST(group + "register")
    Call<Response<User>> register(@Part("email") RequestBody email, @Part("password") RequestBody password,
                                  @Part("user_type") RequestBody user_type, @Part("nombre") RequestBody nombre,
                                  @Part("apellidos") RequestBody apellidos, @Part("dni") RequestBody dni,
                                  @Part("telefono") RequestBody telefono, @Part("fecha_nacimiento") RequestBody fecha_nacimiento,
                                  @Part("sexo") RequestBody sexo, @Part("grupo_sanguineo") RequestBody grupo_sanguineo,
                                  @Part("alergias") RequestBody alergias, @Part("otros") RequestBody otros);

    @Multipart
    @POST(group + "update")
    Call<Response<User>> updateData(@Part("id") RequestBody id, @Part("nombre") RequestBody nombre,
                                  @Part("apellidos") RequestBody apellidos, @Part("dni") RequestBody dni,
                                  @Part("telefono") RequestBody telefono, @Part("fecha_nacimiento") RequestBody fecha_nacimiento,
                                  @Part("sexo") RequestBody sexo, @Part("grupo_sanguineo") RequestBody grupo_sanguineo,
                                  @Part("alergias") RequestBody alergias, @Part("otros") RequestBody otros);

    @GET(group + "id/{email}")
    Call<Response<User>> getId(@Path("email") String email);

    @GET(group + "data/{id}")
    Call<Response<User>> getData(@Path("id") String id);

    @GET(group + "userData/{id}")
    Call<Response<User>> getUserData(@Path("id") String id);

    @GET(group + "medicalData/{id}")
    Call<Response<User>> getMedicalData(@Path("id") String id);

    @Multipart
    @POST(group + subgroup + "token")
    Call<Response<String>> setToken(@Part("id") RequestBody id, @Part("token") RequestBody token);

    @Multipart
    @POST(group + subgroup + "location")
    Call<Response<String>> setLocation(@Part("id") RequestBody id, @Part("latitude") RequestBody latitude,
                                       @Part("longitude") RequestBody longitude);

    @Multipart
    @POST(group + subgroup + "radius")
    Call<Response<String>> setRadius(@Part("id") RequestBody id, @Part("radius") RequestBody radius);

    @GET(group + subgroup + "radius/{id}")
    Call<Response<String>> getRadius(@Path("id") String id);
}
