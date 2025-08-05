package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.Folder;

import java.util.List;

public interface FolderApiInterface {

    @GET("folders/id/{id}")
    Call<Folder> getById(@Path("id") Long id);

    @GET("folders/name/{name}")
    Call<Folder> getByName(@Path("name") String name);

    @GET("folders/all")
    Call<List<Folder>> getAll();

    @GET("folders/all-by/product-group-id/{id}")
    Call<List<Folder>> getAllByProductGroupId(@Path("id") Long id);

    @GET("folders/all-by-text/{text}")
    Call<List<Folder>> getAllByText(@Path("text") String text);

    @POST("folders/create")
    Call<Folder> create(@Body Folder entity);

    @PUT("folders/update")
    Call<Void> update(@Body Folder entity);

    @DELETE("folders/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
