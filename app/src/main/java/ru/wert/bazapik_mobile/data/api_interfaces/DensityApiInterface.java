package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Density;

import java.util.List;

public interface DensityApiInterface {

    @GET("densities/id/{id}")
    Call<Density> getById(@Path("id") Long id);

    @GET("densities/name/{name}")
    Call<Density> getByName(@Path("name") String name);

    @GET("densities/value/{val}")
    Call<Density> getByValue(@Path("val") double value);

    @GET("densities/all")
    Call<List<Density>> getAll();

    @GET("densities/all-by-text/{text}")
    Call<List<Density>> getAllByText(@Path("text") String text);

    @POST("densities/create")
    Call<Density> create(@Body Density entity);

    @PUT("densities/update")
    Call<Void> update(@Body Density entity);

    @DELETE("densities/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
