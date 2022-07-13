package ru.wert.bazapik_mobile.data.serviceNew.files;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.PicApiInterface;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class PicRetrofitService {

    public static void create(PicRetrofitService.IPicCreator cl, Context context, Uri uri, String ext) {

        Pic newPic = new Pic();
        newPic.setExtension(ext);
        newPic.setUser(CURRENT_USER);
        newPic.setTime(ThisApplication.getCurrentTime());

        PicApiInterface api = RetrofitClient.getInstance().getRetrofit().create(PicApiInterface.class);
        Call<Pic> call = api.create(newPic);
        call.enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(@NonNull Call<Pic> call, @NonNull Response<Pic> response) {
                if (response.isSuccessful()) {
                    cl.doWhenPicIsCreated(response, uri);
                }

            }

            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                new WarningDialog1().show(context, "Ошибка!", "Не удалось загрузить изображение");
            }
        });
    }

    public interface IPicCreator {
        void doWhenPicIsCreated(Response<Pic> response, Uri uri);
    }
}