package ru.wert.bazapik_mobile.data.api_interfaces;


import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.AnyPart;

import java.util.List;

public interface AnyPartApiInterface {

    @GET("parts/id/{id}")
    Call<AnyPart> getById(@Path("id") Long id);

    @GET("parts/name/{name}")
    Call<AnyPart> getByName(@Path("name") String name);

    @GET("parts/all")
    Call<List<AnyPart>> getAll();

    @GET("parts/all-by-text/{text}")
    Call<List<AnyPart>> getAllByText(@Path("text") String text);

    @POST("parts/create")
    Call<AnyPart> create(@Body AnyPart entity);

    @PUT("parts/update")
    Call<Void> update(@Body AnyPart entity);

    @DELETE("parts/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
