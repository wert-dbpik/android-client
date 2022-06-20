package ru.wert.bazapik_mobile.data.api_interfaces;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Product;

import java.util.List;
import java.util.Set;

public interface DraftApiInterface {


    @GET("drafts/find-drafts-by-mask/{folder}/{mask}")
    Call<List<String>> findDraftsByMask(@Path("folder") String folder, @Path("mask") String mask);

    @GET("drafts/find-by-folder/{folderId}")
    Call<List<Draft>> findAllByFolder(@Path("folderId") Long folderId);

    @Streaming
    @GET("drafts/download/{path}/{fileName}")
    Call<ResponseBody> download(@Path("path") String path, @Path("fileName") String fileName);

    @Multipart
    @POST("drafts/upload/{path}")
    Call<Void> upload(@Path("path") String path, @Part MultipartBody.Part file);

    @PUT("drafts/rename-dir/{oldPath}/{newPath}")
    Call<Draft> renameDir(@Path("oldPath") String oldPath, @Path("newPath") String newPath);

    @DELETE("drafts/delete-dir/{path}")
    Call<Draft> deleteDir(@Path("path") String path);

    @DELETE("drafts/delete-file/{path}/{fileName:.+}")
    Call<Draft> deleteFile(@Path("path") String path, @Path("fileName:.+") String fileName);

    //==================================================

    @GET("drafts/products-in-draft/{draftId}")
    Call<Set<Product>> getProducts(@Path("draftId") Long draftId);

    @GET("drafts/add-product-in-draft/{draftId}/{productId}")
    Call<Set<Product>> addProduct(@Path("draftId") Long draftId, @Path("productId") Long productId);

    @GET("drafts/remove-product-in-draft/{draftId}/{productId}")
    Call<Set<Product>> removeProduct(@Path("draftId") Long draftId, @Path("productId") Long productId);

    @GET("drafts/id/{id}")
    Call<Draft> getById(@Path("id") Long id);

    @GET("drafts/passport-id/{id}")
    Call<List<Draft>> getByPassportId(@Path("id") Long id);

    @GET("drafts/all")
    Call<List<Draft>> getAll();

    @GET("drafts/all-by-text/{text}")
    Call<List<Draft>> getAllByText(@Path("text") String text);

    @POST("drafts/create")
    Call<Draft> create(@Body Draft entity);

    @PUT("drafts/update")
    Call<Void> update(@Body Draft entity);

    @DELETE("drafts/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
