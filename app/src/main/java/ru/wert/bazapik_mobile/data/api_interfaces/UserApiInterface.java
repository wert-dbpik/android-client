package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.User;

import java.util.List;

public interface UserApiInterface {

    @GET("users/id/{id}")
    Call<User> getById(@Path("id") Long id);

    @GET("users/name/{name}")
    Call<User> getByName(@Path("name") String name);

    @GET("users/pass/{pass}")
    Call<User> getByPassword(@Path("pass") String pass);

    @GET("users/all")
    Call<List<User>> getAll();

    @GET("users/all-by-text/{text}")
    Call<List<User>> getAllByText(@Path("text") String text);

    @POST("users/create")
    Call<User> create(@Body User p);

    @PUT("users/update")
    Call<Void> update(@Body User p);

    @DELETE("users/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
