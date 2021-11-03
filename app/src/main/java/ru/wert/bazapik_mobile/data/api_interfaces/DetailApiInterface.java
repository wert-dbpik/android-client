package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.Detail;

import java.util.List;

public interface DetailApiInterface {

    @GET("details/id/{id}")
    Call<Detail> getById(@Path("id") Long id);

    @GET("details/passport-id/{id}")
    Call<Detail> getByPassportId(@Path("id") Long id);

    @GET("details/all-by/folder-id/{id}")
    Call<List<Detail>> getAllByFolderId(@Path("id") Long id);

    @GET("details/all")
    Call<List<Detail>> getAll();

    @GET("details/all-by-text/{text}")
    Call<List<Detail>> getAllByText(@Path("text") String text);

    @GET("details/all-by/draft-id/{id}")
    Call<List<Detail>> getAllByDraftId(@Path("id") Long id);

    @POST("details/create")
    Call<Detail> create(@Body Detail entity);

    @PUT("details/update")
    Call<Void> update(@Body Detail entity);

    @DELETE("details/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
