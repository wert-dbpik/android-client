package ru.wert.tubus_mobile.data.api_interfaces;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/*
Отдельно выделенный интерфейс использует методы из DraftsApiInterface
 */
public interface FileApiInterface {

    @Streaming
    @GET("files/download/{path}/{fileName}")
    Call<ResponseBody> download(@Path("path") String path, @Path("fileName") String fileName);

    /**
     * Загрузка файла на сервер
     * @param folder, String - имя папки хранения на сервере ("pics", "drafts" "excels")
     * @param file - собственно файл
     * @return
     */
    @Multipart
    @POST("files/upload/{folder}")
    Call<Void> upload(@Path("folder") String folder, @Part MultipartBody.Part file);

}
