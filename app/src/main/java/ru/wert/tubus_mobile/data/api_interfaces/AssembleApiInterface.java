package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.Assemble;

import java.util.List;

public interface AssembleApiInterface {

    @GET("assembles/id/{id}")
    Call<Assemble> getById(@Path("id") Long id);

    @GET("assembles/passport-id/{id}")
    Call<Assemble> getByPassportId(@Path("id") Long id);

    @GET("assembles/all-by/folder-id/{id}")
    Call<List<Assemble>> getAllByFolderId(@Path("id") Long id);

    @GET("assembles/all")
    Call<List<Assemble>> getAll();

    @GET("assembles/all-by-text/{text}")
    Call<List<Assemble>> getAllByText(@Path("text") String text);

    @GET("assembles/all-by/draft-id/{id}")
    Call<List<Assemble>> getAllByDraftId(@Path("id") Long id);

    @POST("assembles/create")
    Call<Assemble> create(@Body Assemble entity);

    @PUT("assembles/update")
    Call<Void> update(@Body Assemble entity);

    @DELETE("assembles/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
