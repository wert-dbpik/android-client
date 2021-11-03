package ru.wert.bazapik_mobile.data.api_interfaces;


import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.AnyPartType;

import java.util.List;

public interface AnyPartTypeApiInterface {

    @GET("part-groups/id/{id}")
    Call<AnyPartType> getById(@Path("id") Long id);

    @GET("part-groups/name/{name}")
    Call<AnyPartType> getByName(@Path("name") String name);

    @GET("part-groups/all")
    Call<List<AnyPartType>> getAll();

    @GET("part-groups/all-by-text/{text}")
    Call<List<AnyPartType>> getAllByText(@Path("text") String text);

    @POST("part-groups/create")
    Call<AnyPartType> create(@Body AnyPartType entity);

    @PUT("part-groups/update")
    Call<Void> update(@Body AnyPartType entity);

    @DELETE("part-groups/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
