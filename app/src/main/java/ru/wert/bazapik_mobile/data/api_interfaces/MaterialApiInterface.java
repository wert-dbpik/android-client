package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Material;

import java.util.List;

public interface MaterialApiInterface {

    @GET("materials/id/{id}")
    Call<Material> getById(@Path("id") Long id);

    @GET("materials/name/{name}")
    Call<Material> getByName(@Path("name") String name);

    @GET("materials/all")
    Call<List<Material>> getAll();

    @GET("materials/all-by-text/{text}")
    Call<List<Material>> getAllByText(@Path("text") String text);

    @POST("materials/create")
    Call<Material> create(@Body Material entity);

    @PUT("materials/update")
    Call<Void> update(@Body Material entity);

    @DELETE("materials/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
