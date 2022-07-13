package ru.wert.bazapik_mobile.data.serviceNew.files;

import android.content.Context;
import android.util.Log;

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
     * Добавить комментарий
     */
    public static void create(IRemarkCreator cl, Context context, Remark remark) {

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.create(remark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(@NonNull Call<Remark> call, @NonNull Response<Remark> response) {
                if (response.isSuccessful()) {
                    cl.doWhenRemarkIsCreated(response);

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

    public interface IRemarkCreator{
        void doWhenRemarkIsCreated(Response<Remark> response);
    }

    //Изменить комментарий
    public static void  change(IRemarkCreator cl, Context context, Remark changedRemark) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.update(changedRemark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    cl.doWhenRemarkIsCreated(response);

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

    public interface IRemarkChanger {
        void doWhenRemarkHasBeenChanged(Response<Remark> response);
    }
}
