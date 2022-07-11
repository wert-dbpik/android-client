package ru.wert.bazapik_mobile.data.api_interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.wert.bazapik_mobile.data.models.AppLog;
import ru.wert.bazapik_mobile.data.models.Remark;

public interface PicApiInterface {

    @POST("pics/create")
    Call<AppLog> create(@Body AppLog p);

    @GET("pics/id/{id}")
    Call<AppLog> getById(@Path("id") Long id);
    
    @GET("pics/all")
    Call<List<AppLog>> getAll();
    
    @DELETE("pics/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);
    
    //=============  КАРТИНКИ И КОММЕНТАРИИ =============================

    @GET("pics/remarks-in-pic/{picId}")
    Call<Set<Remark>> getRemarks(@Path("picId") Long picId);

    @GET("pics/add-remark-in-pic/{picId}/{remarkId}")
    Call<Set<Remark>> addRemark(@Path("picId") Long picId, @Path("remarkId") Long remarkId);

    @GET("pics/remove-remark-in-pic/{picId}/{remarkId}")
    Call<Set<Remark>> removeRemark(@Path("picId") Long picId, @Path("remarkId") Long remarkId);

}
