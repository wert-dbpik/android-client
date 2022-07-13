package ru.wert.bazapik_mobile.data.serviceRETROFIT;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class RemarkRetrofitService {
    public static String TAG = "RemarkRetrofitService";

    /**
     * Найти по пасспорту
     */
    public static void findByPassportId(IRemarkFindByPassportId cl, Context context, Long passId) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<List<Remark>> call =  api.getAllByPassportId(passId);
        call.enqueue(new Callback<List<Remark>>() {
            @Override
            public void onResponse(Call<List<Remark>> call, Response<List<Remark>> response) {
                if (response.isSuccessful()) {
                    cl.doWhenRemarkHasBeenFoundByPassportId(response);
                } else {
                    new WarningDialog1().show(context, "Внимание!", "Проблемы на линии!");
                }
            }

            @Override
            public void onFailure(Call<List<Remark>> call, Throwable t) {
                new WarningDialog1().show(context, "Внимание!", "Проблемы на линии!");
            }
        });
    }

    public interface IRemarkFindByPassportId {
        void doWhenRemarkHasBeenFoundByPassportId(Response<List<Remark>> response);
    }

    /**
     * Добавить комментарий
     */
    public static void create(IRemarkCreate cl, Context context, Remark remark) {

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.create(remark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(@NonNull Call<Remark> call, @NonNull Response<Remark> response) {
                if (response.isSuccessful()) {
                    cl.doWhenRemarkHasBeenCreated(response);

                } else {
                    Log.d(TAG, String.format("Не удалось сохранить запись, %s", response.message()));
                    new WarningDialog1().show(context, "Ошибка!", "Не удалось сохранить запись");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                Log.d(TAG, String.format("Не удалось сохранить запись, %s", t.getMessage()));
                new WarningDialog1().show(context, "Ошибка!", "Не удалось сохранить запись");
            }
        });
    }

    public interface IRemarkCreate {
        void doWhenRemarkHasBeenCreated(Response<Remark> response);
    }

    //Изменить комментарий
    public static void  change(RemarkRetrofitService.IRemarkChange cl, Context context, Remark changedRemark) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.update(changedRemark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    cl.doWhenRemarkHasBeenChanged(response);

                } else {
                    Log.d(TAG, String.format("Не удалось изменить запись, %s", response.message()));
                    new WarningDialog1().show(context, "Ошибка!","Не удалось сохранить запись");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                Log.d(TAG, String.format("Не удалось изменить запись, %s", t.getMessage()));
                new WarningDialog1().show(context, "Ошибка!", "Не удалось сохранить запись");
            }
        });
    }

    public interface IRemarkChange {
        void doWhenRemarkHasBeenChanged(Response<Remark> response);
    }
}
