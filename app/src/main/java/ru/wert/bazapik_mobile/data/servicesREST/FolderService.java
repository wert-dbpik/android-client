package ru.wert.bazapik_mobile.data.servicesREST;

import android.content.Context;

import retrofit2.Call;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FolderApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.IFolderService;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;

public class FolderService implements IFolderService, ItemService<Folder> {

    private static FolderService instance;
    private FolderApiInterface api;
    private Context context;

    public FolderService(Context context) {
        this.context = context;
        ThisApplication.FOLDER_SERVICE = this;
        api = RetrofitClient.getInstance().getRetrofit().create(FolderApiInterface.class);
    }

    public FolderApiInterface getApi() {
        return api;
    }


    @Override
    public Folder findById(Long id) {
        try {
            Call<Folder> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Folder findByName(String name) {
        try {
            Call<Folder> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Folder findByDecNumber(String number) {
        try {
            Call<Folder> call = api.getByDecNumber(number);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Folder> findAll() {
        try {
            Call<List<Folder>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Folder> findAllByText(String text) {
        try {
            Call<List<Folder>> call = api.getAllByText(text);;
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Folder save(Folder entity) {
        try {
            Call<Folder> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean update(Folder entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {

        }
        return false;
    }

    @Override
    public boolean delete(Folder entity) {
        Long id = entity.getId();
        try {
            Call<Void> call = api.deleteById(id);
            if(call.execute().code() == 200)
                return true;
        } catch (IOException e) {

        }
        return false;
    }

}
