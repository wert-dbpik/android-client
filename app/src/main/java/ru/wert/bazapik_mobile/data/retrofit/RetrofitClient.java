package ru.wert.bazapik_mobile.data.retrofit;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.java.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.wert.bazapik_mobile.MainActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;

import static ru.wert.bazapik_mobile.constants.Consts.DATA_BASE_URL;

@Log
public class RetrofitClient extends Application {
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


        //На всякий случай
        if(BASE_URL.equals("")){
            String ip = BaseActivity.getProp("IP");
            String port = BaseActivity.getProp("PORT");
            BASE_URL = "http://" + ip + ":" + port + "/";
        }

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build()) // логгирование ответа
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



