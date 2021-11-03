package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Prefix;

import java.util.List;

public interface PrefixApiInterface {

    @GET("prefixes/id/{id}")
    Call<Prefix> getById(@Path("id") Long id);

    @GET("prefixes/name/{name}")
    Call<Prefix> getByName(@Path("name") String name);

    @GET("prefixes/all")
    Call<List<Prefix>> getAll();

    @GET("prefixes/all-by-text/{text}")
    Call<List<Prefix>> getAllByText(@Path("text") String text);

    @POST("prefixes/create")
    Call<Prefix> create(@Body Prefix entity);

    @PUT("prefixes/update")
    Call<Void> update(@Body Prefix entity);

    @DELETE("prefixes/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
