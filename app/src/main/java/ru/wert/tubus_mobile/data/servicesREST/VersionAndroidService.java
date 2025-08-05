package ru.wert.tubus_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.tubus_mobile.data.api_interfaces.VersionAndroidApiInterface;
import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.VersionAndroid;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.service_interfaces.IVersionAndroidService;
import ru.wert.tubus_mobile.data.util.BLlinks;


import java.io.IOException;
import java.util.List;

public class VersionAndroidService implements IVersionAndroidService, ItemService<VersionAndroid> {

    private static VersionAndroidService instance;
    private VersionAndroidApiInterface api;

    private VersionAndroidService() {
        BLlinks.versionAndroidService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(VersionAndroidApiInterface.class);
    }

    public VersionAndroidApiInterface getApi() {
        return api;
    }

    public static VersionAndroidService getInstance() {
        if (instance == null)
            return new VersionAndroidService();
        return instance;
    }

    @Override
    public VersionAndroid findById(Long id) {
        try {
            Call<VersionAndroid> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public VersionAndroid findByName(String name) {
        try {
            Call<VersionAndroid> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public List<VersionAndroid> findAll() {
        try {
            Call<List<VersionAndroid>> call = api.getAll();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<VersionAndroid> findAllByText(String text) {
        //не используется
        return null;
    }

    @Override
    public VersionAndroid save(VersionAndroid entity) {
        try {
            Call<VersionAndroid> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(VersionAndroid entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(VersionAndroid entity) {
        Long id = entity.getId();
        try {
            Call<Void> call = api.deleteById(id);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
