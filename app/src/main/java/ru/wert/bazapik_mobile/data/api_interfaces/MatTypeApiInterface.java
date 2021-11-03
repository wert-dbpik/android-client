package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.MatType;

import java.util.List;

public interface MatTypeApiInterface {

    @GET("mat-types/id/{id}")
    Call<MatType> getById(@Path("id") Long id);

    @GET("mat-types/name/{name}")
    Call<MatType> getByName(@Path("name") String name);

    @GET("mat-types/all")
    Call<List<MatType>> getAll();

    @GET("mat-types/all-by-text/{text}")
    Call<List<MatType>> getAllByText(@Path("text") String text);

    @POST("mat-types/create")
    Call<MatType> create(@Body MatType entity);

    @PUT("mat-types/update")
    Call<Void> update(@Body MatType entity);

    @DELETE("mat-types/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
