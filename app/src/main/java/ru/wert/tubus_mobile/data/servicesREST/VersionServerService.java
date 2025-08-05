package ru.wert.tubus_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.tubus_mobile.data.api_interfaces.VersionServerApiInterface;
import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.VersionServer;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.service_interfaces.IVersionServerService;
import ru.wert.tubus_mobile.data.util.BLlinks;


import java.io.IOException;
import java.util.List;

public class VersionServerService implements IVersionServerService, ItemService<VersionServer> {

    private static VersionServerService instance;
    private VersionServerApiInterface api;

    private VersionServerService() {
        BLlinks.versionServerService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(VersionServerApiInterface.class);
    }

    public VersionServerApiInterface getApi() {
        return api;
    }

    public static VersionServerService getInstance() {
        if (instance == null)
            return new VersionServerService();
        return instance;
    }

    @Override
    public VersionServer findById(Long id) {
        try {
            Call<VersionServer> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public VersionServer findByName(String name) {
        try {
            Call<VersionServer> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public List<VersionServer> findAll() {
        try {
            Call<List<VersionServer>> call = api.getAll();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<VersionServer> findAllByText(String text) {
        //не используется
        return null;
    }

    @Override
    public VersionServer save(VersionServer entity) {
        try {
            Call<VersionServer> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(VersionServer entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(VersionServer entity) {
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
