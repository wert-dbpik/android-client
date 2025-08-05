package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.VersionDesktop;

import java.util.List;

public interface VersionDesktopApiInterface {

    @GET("versions_desktop/id/{id}")
    Call<VersionDesktop> getById(@Path("id") Long id);

    @GET("versions_desktop/name/{name}")
    Call<VersionDesktop> getByName(@Path("name") String name);

    @GET("versions_desktop/all")
    Call<List<VersionDesktop>> getAll();

    @POST("versions_desktop/create")
    Call<VersionDesktop> create(@Body VersionDesktop p);

    @PUT("versions_desktop/update")
    Call<Void> update(@Body VersionDesktop p);

    @DELETE("versions_desktop/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
