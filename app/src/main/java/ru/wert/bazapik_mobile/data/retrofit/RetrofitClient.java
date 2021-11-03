package ru.wert.bazapik_mobile.data.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Log
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    static String BASE_URL = "";
    private static RetrofitClient mInstance;
    private static Retrofit mRetrofit;
    private Gson gson;

    /**
     * Приватный конструктор
     */
    private RetrofitClient() {

        gson = new GsonBuilder()
                .setLenient()
                .create();

        //Перехватчик для логгирования запросов и ответов
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .addInterceptor(interceptor);

//        Interceptor interceptor1 = chain -> {
//            Request.Builder requestBuilder = chain.request().newBuilder();
//            requestBuilder.header("Content-Type", "application/json");
//            requestBuilder.header("Accept", "application/json");
//            return chain.proceed(requestBuilder.build());
//        };
//
//        OkHttpClient.Builder client1 = new OkHttpClient.Builder()
//                .addInterceptor(interceptor1);

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .client(client.build()) // логгирование ответа
//                .client(client1.build()) // json forever!
                .build();
    }

    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }


    public static void setBASE_URL(String baseUrl) {
        if (!baseUrl.equals(BASE_URL)) {
            BASE_URL = baseUrl;
            new RetrofitClient();
        }

    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }
}



