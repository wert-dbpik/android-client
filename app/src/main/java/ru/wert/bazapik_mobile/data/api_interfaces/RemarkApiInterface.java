package ru.wert.bazapik_mobile.data.api_interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import ru.wert.bazapik_mobile.data.models.Remark;

public interface RemarkApiInterface {

    @GET("remarks/id/{id}")
    Call<Remark> getById(@Path("id") Long id);

    @GET("remarks/all")
    Call<List<Remark>> getAll();

    @GET("remarks/passport-id/{id}")
    Call<List<Remark>> getAllByPassportId(@Path("id") Long id);

    @POST("remarks/create")
    Call<Remark> create(@Body Remark entity);

    @PUT("remarks/update")
    Call<Void> update(@Body Remark entity);

    @DELETE("remarks/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
