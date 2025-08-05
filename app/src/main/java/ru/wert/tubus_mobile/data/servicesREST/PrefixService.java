package ru.wert.tubus_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.tubus_mobile.data.api_interfaces.PrefixApiInterface;
import ru.wert.tubus_mobile.data.service_interfaces.IPrefixService;
import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Prefix;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;

public class PrefixService implements IPrefixService, ItemService<Prefix> {

    private static PrefixService instance;
    private PrefixApiInterface api;

    private PrefixService() {
        BLlinks.prefixService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(PrefixApiInterface.class);
    }

    public PrefixApiInterface getApi() {
        return api;
    }

    public static PrefixService getInstance() {
        if (instance == null)
            return new PrefixService();
        return instance;
    }

    @Override
    public Prefix findById(Long id) {
        try {
            Call<Prefix> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Prefix findByName(String name) {
        try {
            Call<Prefix> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Prefix> findAll() {
        try {
            Call<List<Prefix>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Prefix> findAllByText(String text) {
        try {
            Call<List<Prefix>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Prefix save(Prefix entity) {
        try {
            Call<Prefix> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(Prefix entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Prefix entity) {
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
