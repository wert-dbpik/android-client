package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.TechProcess;

import java.util.List;

public interface ProcessApiInterface {

    @GET("processes/id/{id}")
    Call<TechProcess> getById(@Path("id") Long id);

    @GET("processes/name/{name}")
    Call<TechProcess> getByName(@Path("name") String name);

    @GET("processes/all")
    Call<List<TechProcess>> getAll();

    @GET("processes/all-by-text/{text}")
    Call<List<TechProcess>> getAllByText(@Path("text") String text);

    @POST("processes/create")
    Call<TechProcess> create(@Body TechProcess entity);

    @PUT("processes/update")
    Call<Void> update(@Body TechProcess entity);

    @DELETE("processes/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
