package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Coat;

import java.util.List;

public interface CoatApiInterface {

    @GET("coats/id/{id}")
    Call<Coat> getById(@Path("id") Long id);

    @GET("coats/name/{name}")
    Call<Coat> getByName(@Path("name") String name);

    @GET("coats/all")
    Call<List<Coat>> getAll();

    @GET("coats/all-by-text/{text}")
    Call<List<Coat>> getAllByText(@Path("text") String text);

    @POST("coats/create")
    Call<Coat> create(@Body Coat entity);

    @PUT("coats/update")
    Call<Void> update(@Body Coat entity);

    @DELETE("coats/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
