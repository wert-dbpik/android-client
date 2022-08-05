package ru.wert.bazapik_mobile.data.serviceRETROFIT;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Pic;
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
                    new WarningDialog1().show(context, "Внимание!", "Требуемый пасспорт не найден!");
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
    public static void update(RemarkRetrofitService.IRemarkChange cl, Context context, Remark changedRemark) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.update(changedRemark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    cl.doWhenRemarkHasBeenChanged(response);

                } else {
                    Log.d(TAG, String.format("Не удалось изменить запись, %s", response.message()));
                    new WarningDialog1().show(context, "Ошибка!","Не удалось изменить запись");
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

    public static void addPics(RemarkRetrofitService.IRemarkAddPic cl, Context context, Remark remark, List<Pic> pics){
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        List<String> picIds = pics.stream().flatMap(p -> Stream.of(String.valueOf(p.getId()))).collect(Collectors.toList());
        Call<Set<Pic>> call = api.addPics(picIds, remark.getId());
        call.enqueue(new Callback<Set<Pic>>() {
            @SneakyThrows
            @Override
            public void onResponse(Call<Set<Pic>> call, Response<Set<Pic>> response) {
                if (response.isSuccessful()) {
                    cl.doWhenRemarkHasBeenAddedPic(response);
                } else {

                    Log.e(TAG, String.format("Не удалось добавить картинку в комментарий, %s", response.errorBody().string()));
                    new WarningDialog1().show(context, "Ошибка!", "Не удалось добавить картинку в комментарий");
                }
            }
            @Override
            public void onFailure(Call<Set<Pic>> call, Throwable t) {
                Log.e(TAG, String.format("Не удалось добавить картинку в комментарий, %s", t.getMessage()));
                new WarningDialog1().show(context, "Ошибка!", "Не удалось добавить картинку в комментарий");
            }
        });
    }

    public interface IRemarkAddPic {
        void doWhenRemarkHasBeenAddedPic(Response<Set<Pic>> response);
    }
}
