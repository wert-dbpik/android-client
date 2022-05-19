package ru.wert.bazapik_mobile.data.servicesREST;

import android.app.Application;

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
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Product;
import ru.wert.bazapik_mobile.data.util.BLlinks;
import ru.wert.bazapik_mobile.data.service_interfaces.IDraftService;

public class DraftService extends Application implements IDraftService {

    private DraftApiInterface api;

    public DraftService() {
        ThisApplication.DRAFT_SERVICE = this;
        api = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
    }

    public DraftApiInterface getApi() {
        return api;
    }

    //=====================================================================================
    @Override
    public List<String> findDraftsByMask(String folder, String mask){
        try {
            Call<List<String>> call = api.findDraftsByMask(folder, mask);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    @Override
    public boolean download(String path, String fileName, String ext, String tempDir) {
        //ext уже с точкой
        String file = fileName + ext;
        try {
            Call<ResponseBody> call = api.download(path, file);
            Response<ResponseBody> r = call.execute();
            if (r.isSuccessful()) {

//                if (ext.toLowerCase().equals(".pdf")) {
                InputStream inputStream = r.body().byteStream();
                try (OutputStream outputStream = new FileOutputStream(tempDir + "/" + fileName  + ext)) {
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

    @Override
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

    @Override
    public Set<Product> findProducts(Draft draft) {
        try {
            Call<Set<Product>> call = api.getProducts(draft.getId());
            Set<Product> res = call.execute().body();
            if (res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Product> addProducts(Draft draft, Product product) {
        try {
            Call<Set<Product>> call = api.addProduct(draft.getId(), product.getId());
            Set<Product> res = call.execute().body();
            if (res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Product> removeProducts(Draft draft, Product product) {
        try {
            Call<Set<Product>> call = api.removeProduct(draft.getId(), product.getId());
            Set<Product> res = call.execute().body();
            if (res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Draft> findAllByFolder(Folder folder) {
        try {
            Call<Set<Draft>> call = api.findAllByFolder(folder.getId());
            Set<Draft> res = call.execute().body();
            if (res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //========================================================================

    @Override
    public Draft findById(Long id) {
        try {
            Call<Draft> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Draft> findByPassportId(Long id) {
        try {
            Call<List<Draft>> call = api.getByPassportId(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<Draft> findAll() {
        try {
            Call<List<Draft>> call = api.getAll();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<Draft> findAllByText(String text) {
        try {
            Call<List<Draft>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Draft save(Draft entity) {
        try {
            Call<Draft> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(Draft entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Draft entity) {
        Long id = entity.getId();
        try {
            Call<Void> call = api.deleteById(id);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            return null;
        }

        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}
