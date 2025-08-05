package ru.wert.tubus_mobile.data.retrofit;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.java.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ru.wert.tubus_mobile.ThisApplication.getProp;

import java.util.concurrent.TimeUnit;

@Log
public class RetrofitClient extends Application {
    private static final long CONNECT_TIMEOUT = 30; // seconds
    private static final long READ_TIMEOUT = 30;    // seconds
    private static final long WRITE_TIMEOUT = 30;   // seconds
    private static final String TAG = "RetrofitClient";
    public static String BASE_URL = "";
    private static RetrofitClient mInstance;
    private static Retrofit mRetrofit;
    private final Gson gson;

    private RetrofitClient() {

        gson = new GsonBuilder()
                .setLenient()
                .create();

        //Перехватчик для логгирования запросов и ответов
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();


        //На всякий случай
        if (BASE_URL.equals("")) {
            String ip = getProp("IP");
            String port = getProp("PORT");
            BASE_URL = "http://" + ip + ":" + port + "/";
        }

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(new NullOnEmptyConverterFactory()) //Исправляет исключение на null, когда приходит пустое тело
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient) // логгирование ответа
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

    public Retrofit getRetrofit(){
        return mRetrofit;
    }

}



