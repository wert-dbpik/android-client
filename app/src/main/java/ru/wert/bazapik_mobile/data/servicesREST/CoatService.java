package ru.wert.bazapik_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.CoatApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.ICoatService;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Coat;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;

public class CoatService implements ICoatService, ItemService<Coat> {

    private static CoatService instance;
    private CoatApiInterface api;

    private CoatService() {
        BLlinks.coatService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(CoatApiInterface.class);
    }

    public CoatApiInterface getApi() {
        return api;
    }

    public static CoatService getInstance() {
        if (instance == null)
            return new CoatService();
        return instance;
    }

    @Override
    public Coat findById(Long id) {
        try {
            Call<Coat> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Coat findByName(String name) {
        try {
            Call<Coat> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Coat> findAll() {
        try {
            Call<List<Coat>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Coat> findAllByText(String text) {
        try {
            Call<List<Coat>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(Coat entity) {
        try {
            Call<Coat> call = api.create(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Coat entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Coat entity) {
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
