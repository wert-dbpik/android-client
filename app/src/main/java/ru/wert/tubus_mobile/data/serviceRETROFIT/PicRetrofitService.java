package ru.wert.tubus_mobile.data.serviceRETROFIT;

import android.content.Context;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.tubus_mobile.data.api_interfaces.PicApiInterface;
import ru.wert.tubus_mobile.data.models.Pic;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.warnings.WarningDialog1;

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
