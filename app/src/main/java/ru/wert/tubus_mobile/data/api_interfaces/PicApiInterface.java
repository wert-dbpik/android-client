package ru.wert.tubus_mobile.data.api_interfaces;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.wert.tubus_mobile.data.models.Pic;
import ru.wert.tubus_mobile.data.models.Remark;

public interface PicApiInterface {

    @POST("pics/create")
    Call<Pic> create(@Body Pic p);

    @GET("pics/id/{id}")
    Call<Pic> getById(@Path("id") Long id);
    
    @GET("pics/all")
    Call<List<Pic>> getAll();
    
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
