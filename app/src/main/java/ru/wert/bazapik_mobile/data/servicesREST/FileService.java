package ru.wert.bazapik_mobile.data.servicesREST;

import static ru.wert.bazapik_mobile.ThisApplication.createProgressDialog;
import static ru.wert.bazapik_mobile.data.servicesREST.DraftService.getBytesFromFile;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import org.apache.pdfbox.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AlertDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.DownloadFileTask;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class FileService extends Application {

    private FileApiInterface api;
    private static FileService instance;
    private static ProgressDialog progressDialog;

    private FileService() {
        ThisApplication.FILE_SERVICE = this;
        api = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);

    }

    public static FileService getInstance() {
        if (instance == null)
            return new FileService();
        return instance;
    }

    public FileApiInterface getApi() {
        return api;
    }

    /**
     * Метод загружает файл с сервера в указанную папку
     * @param dbFolder, String имя папки откуда скачивается файл ("apk", "excels")
     * @param fileName, String  имя файла с расширением (myFile.apk)
     * @param destFolder, String путь до папки, куда происходит скачивание (/storage/emulated/0/Download)
     */
    public void download(String dbFolder, String fileName, String destFolder, Context context) {
        //Перед загрузкой проверяем наличие уже загруженного ранее файла
        File destFile = new File(destFolder + "/" + fileName);
        if (destFile.exists())
            destFile.delete();

        progressDialog = createProgressDialog(context, fileName);
        progressDialog.show();
        Call<ResponseBody> call = api.download(dbFolder, fileName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    final Thread t = new Thread(()->{
                        try (OutputStream outputStream = new FileOutputStream(destFolder + "/" + fileName)) {
                            IOUtils.copy(inputStream, outputStream);
                            progressDialog.dismiss();

                            new Handler(Looper.getMainLooper()).post(()->{
                                new WarningDialog1().show(context,
                                    "Поздравляю!",
                                    String.format("Файл %s успешно загружен в папку Загрузки. Если вы не увидели файл в этой папке - " +
                                            "терпение! Файловой системе требуется время, чтобы обновиться!", fileName));
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
                    t.start();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                new WarningDialog1().show(context,
                        "Ошибка!",
                        "Не удалось загрузить файл. Попробуйте позднее");
            }
        });

    }

    public boolean upload(String fileName, String folder, File draft) throws IOException {
        byte[] draftBytes = getBytesFromFile(draft);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/pdf"), draftBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestBody);
        try {
            Call<Void> call = api.upload(folder, body);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
