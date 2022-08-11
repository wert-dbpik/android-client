package ru.wert.bazapik_mobile.data.serviceRETROFIT;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.PicApiInterface;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class PicRetrofitService {
    private static String TAG = "PicRetrofitService";




    public static void findByPicId(PicRetrofitService.IPicFindByPicId cl, Context context, Long pikId) {

        PicApiInterface api = RetrofitClient.getInstance().getRetrofit().create(PicApiInterface.class);
        Call<Pic> call = api.getById(pikId);
        call.enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(@NonNull Call<Pic> call, @NonNull Response<Pic> response) {
                if (response.isSuccessful()) {
                    cl.doWhenPicHasBeenFoundByPikId(response);
                }
            }

            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                new WarningDialog1().show(context, "Ошибка!", "Не удалось загрузить изображение");
            }
        });
    }
    public interface IPicFindByPicId {
        void doWhenPicHasBeenFoundByPikId(Response<Pic> response);
    }



}
