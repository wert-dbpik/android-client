package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.ProductGroup;

import java.util.List;

public interface ProductGroupApiInterface {

    @GET("product-tree-groups/id/{id}")
    Call<ProductGroup> getById(@Path("id") Long id);

    @GET("product-tree-groups/name/{name}")
    Call<ProductGroup> getByName(@Path("name") String name);

    @GET("product-tree-groups/all")
    Call<List<ProductGroup>> getAll();

    @GET("product-tree-groups/all-by-text/{text}")
    Call<List<ProductGroup>> getAllByText(@Path("text") String text);

    @POST("product-tree-groups/create")
    Call<ProductGroup> create(@Body ProductGroup entity);

    @PUT("product-tree-groups/update")
    Call<Void> update(@Body ProductGroup entity);

    @DELETE("product-tree-groups/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
