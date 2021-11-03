package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Product;

import java.util.List;
import java.util.Set;

public interface ProductApiInterface {

    @GET("products/drafts-in-product/{productId}")
    Call<Set<Draft>> getDrafts(@Path("productId") Long id);

    @GET("products/add-draft-in-product/{productId}/{draftId}")
    Call<Set<Draft>> addDraft(@Path("productId") Long productId, @Path("draftId") Long draftId);

    @GET("products/remove-draft-in-product/{productId}/{draftId}")
    Call<Set<Draft>> removeDraft(@Path("productId") Long productId, @Path("draftId") Long draftId);

    @GET("products/id/{id}")
    Call<Product> getById(@Path("id") Long id);

    @GET("products/passport-id/{id}")
    Call<Product> getByPassportId(@Path("id") Long id);

    @GET("products/all-by/folder-id/{id}")
    Call<List<Product>> getAllByFolderId(@Path("id") Long id);

    @GET("products/all-by/product-group-id/{id}")
    Call<List<Product>> getAllByProductGroupId(@Path("id") Long id);

    @GET("products/all")
    Call<List<Product>> getAll();

    @GET("products/all-by-text/{text}")
    Call<List<Product>> getAllByText(@Path("text") String text);

    @POST("products/create")
    Call<Product> create(@Body Product entity);

    @PUT("products/update")
    Call<Void> update(@Body Product entity);

    @DELETE("products/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
