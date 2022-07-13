package ru.wert.bazapik_mobile.data.serviceNew.files;

import android.content.Context;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class FileRetrofitService {

    public static void uploadFile(IFileUploader cl, Context context, String destFolder, String fileNewName, byte[] draftBytes){

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), draftBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileNewName, requestBody);

        FileApiInterface fileApi = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);
        Call<Void> uploadCall = fileApi.upload(destFolder, body);
        uploadCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cl.doWhenFileHasBeenUploaded();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                new WarningDialog1().show(context, "Ошибка!", "Не удалось загрузить изображение");
            }
        });
    }

    public interface IFileUploader{
        void doWhenFileHasBeenUploaded();
    }
}
