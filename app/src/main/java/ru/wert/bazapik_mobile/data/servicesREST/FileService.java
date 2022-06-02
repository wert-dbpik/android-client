package ru.wert.bazapik_mobile.data.servicesREST;

import android.app.Application;
import android.content.Context;

import org.apache.pdfbox.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Product;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.service_interfaces.IDraftService;

import static ru.wert.bazapik_mobile.data.servicesREST.DraftService.getBytesFromFile;

public class FileService extends Application {

    private FileApiInterface api;
    private static FileService instance;


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


    public boolean download(String path, String fileName, String ext, String destFolder) {
        //ext уже с точкой
        String file = fileName + ext;
        try {
            Call<ResponseBody> call = api.download(path, file);
            Response<ResponseBody> r = call.execute();
            if (r.isSuccessful()) {

//                if (ext.toLowerCase().equals(".pdf")) {
                InputStream inputStream = r.body().byteStream();
                try (OutputStream outputStream = new FileOutputStream(destFolder + "/" + fileName  + ext)) {
                    IOUtils.copy(inputStream, outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                }
                return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
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
