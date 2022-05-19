package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.VersionAndroid;

import java.util.List;

public interface VersionAndroidApiInterface {

    @GET("versions_android/id/{id}")
    Call<VersionAndroid> getById(@Path("id") Long id);

    @GET("versions_android/name/{name}")
    Call<VersionAndroid> getByName(@Path("name") String name);
    
    @GET("versions_android/all")
    Call<List<VersionAndroid>> getAll();
    
    @POST("versions_android/create")
    Call<VersionAndroid> create(@Body VersionAndroid p);

    @PUT("versions_android/update")
    Call<Void> update(@Body VersionAndroid p);

    @DELETE("versions_android/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
