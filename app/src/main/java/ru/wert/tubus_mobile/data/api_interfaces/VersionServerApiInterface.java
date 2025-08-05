package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.VersionServer;

import java.util.List;

public interface VersionServerApiInterface {

    @GET("versions_server/id/{id}")
    Call<VersionServer> getById(@Path("id") Long id);

    @GET("versions_server/name/{name}")
    Call<VersionServer> getByName(@Path("name") String name);

    @GET("versions_server/all")
    Call<List<VersionServer>> getAll();

    @POST("versions_server/create")
    Call<VersionServer> create(@Body VersionServer p);

    @PUT("versions_server/update")
    Call<Void> update(@Body VersionServer p);

    @DELETE("versions_server/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
