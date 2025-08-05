package ru.wert.tubus_mobile.dataPreloading;

import retrofit2.Response;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;

import java.io.IOException;

public class BatchService {
    private static BatchApiService apiService;

    public static BatchResponse loadInitialData() throws IOException {
        if(apiService == null) {
            apiService = RetrofitClient.getInstance().getRetrofit().create(BatchApiService.class);
        }

        Response<BatchResponse> response = apiService.getInitialData(
                 true, true, true, true, true, true, true)
                .execute();

        if(!response.isSuccessful()) {
            String errorBody = response.errorBody() != null ?
                    response.errorBody().string() : "No error body";
            throw new IOException(String.format(
                    "Batch request failed. Code: %d, Error: %s",
                    response.code(), errorBody));
        }

        return response.body();
    }
}

