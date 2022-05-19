package ru.wert.bazapik_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.bazapik_mobile.data.models.AppLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AppLogApiInterface {

    @POST("logs/create")
    Call<AppLog> create(@Body AppLog p);

    @GET("logs/id/{id}")
    Call<AppLog> getById(@Path("id") Long id);

    @GET("logs/all_by_user_id/{id}")
    Call<List<AppLog>> getAllByUserId(Long id);

    @GET("logs/all_by_user_id_and_admin_only_false/{id}")
    Call<List<AppLog>> getAllByUserIdAndAdminOnlyFalse(Long id);

    @GET("logs/all_by_application/{app}")
    Call<List<AppLog>> getAllByApplication(Integer app);

    @GET("logs/all_by_application_and_admin_only_false/{app}")
    Call<List<AppLog>> getAllByApplicationAndAdminOnlyFalse(Integer app);

    @GET("logs/all_by_time_between/{startTime}/{finishTime}")
    Call<List<AppLog>> getAllByTimeBetween(@Path("firstTime") LocalDateTime startTime, @Path("finishTime") LocalDateTime finishTime);

    @GET("logs/all_by_time_between_and_admin_only_false/{startTime}/{finishTime}")
    Call<List<AppLog>> getAllByTimeBetweenAndAdminOnlyFalse(@Path("firstTime") LocalDateTime startTime, @Path("finishTime") LocalDateTime finishTime);

    @GET("logs/all")
    Call<List<AppLog>> getAll();

    @GET("logs/all_admin_only_false")
    Call<List<AppLog>> getAllAdminOnlyFalse();

    @DELETE("logs/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

}
