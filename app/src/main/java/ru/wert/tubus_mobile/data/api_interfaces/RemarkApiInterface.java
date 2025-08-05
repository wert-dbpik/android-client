package ru.wert.tubus_mobile.data.api_interfaces;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import ru.wert.tubus_mobile.data.models.Pic;
import ru.wert.tubus_mobile.data.models.Remark;

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
    Call<Remark> update(@Body Remark entity);

    @DELETE("remarks/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

    //==========  КАРТИНКИ И КОММЕНТАРИИ

    @GET("remarks/pics-in-remark/{remarkId}")
    Call<Set<Pic>> getPics(@Path("remarkId") Long id);

    @PUT("remarks/add-pics-in-remark/{remarkId}")
    Call<Set<Pic>> addPics(@Body List<String> picIds, @Path("remarkId") Long remarkId);

    @DELETE("remarks/remove-pic-in-remark/{remarkId}/{picId}")
    Call<Set<Pic>> removePic(@Path("remarkId") Long remarkId, @Path("picId") Long picId);

}
