package ru.wert.tubus_mobile.dataPreloading;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BatchApiService {
    @GET("batch/init-data")
    Call<BatchResponse> getInitialData(
            @Query("includeUsers") boolean includeUsers,
            @Query("includeRooms") boolean includeRooms,
            @Query("includeRemarks") boolean includeRemarks,
            @Query("includeDrafts") boolean includeDrafts,
            @Query("includeFolders") boolean includeFolders,
            @Query("includeProductGroups") boolean includeProductGroups,
            @Query("includePassports") boolean includePassports
    );

}
