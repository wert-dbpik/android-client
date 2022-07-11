package ru.wert.bazapik_mobile.data.api_interfaces;

import java.util.List;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Product;
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
     * @param newName - имя под которым необходимо сохранить файл, nullable
     * @param file - собственно файл
     * @return
     */
    @Multipart
    @POST("files/upload/{folder}/{newName}")
    Call<Void> upload(@Path("folder") String folder, @Path("newName") String newName, @Part MultipartBody.Part file);

}
