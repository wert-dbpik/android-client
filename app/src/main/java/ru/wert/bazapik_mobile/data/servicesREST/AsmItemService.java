package ru.wert.bazapik_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.AsmItemApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.IAsmItemService;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AsmItem;
import ru.wert.bazapik_mobile.data.garbage.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;

public class AsmItemService implements IAsmItemService, ItemService<AsmItem> {

    private static AsmItemService instance;
    private AsmItemApiInterface api;

    private AsmItemService() {
        BLlinks.asmItemService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(AsmItemApiInterface.class);
    }

    public AsmItemApiInterface getApi() {
        return api;
    }

    public static AsmItemService getInstance() {
        if (instance == null)
            return new AsmItemService();
        return instance;
    }

    @Override
    public AsmItem findById(Long id) {
        try {
            Call<AsmItem> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AsmItem findByName(String name) {
        try {
            Call<AsmItem> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AsmItem> findAll() {
        try {
            Call<List<AsmItem>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Метод заглушка - поиск по тексту не работает
     */
    @Override
    public List<AsmItem> findAllByText(String text) {
        try {
            Call<List<AsmItem>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(AsmItem entity) {
        try {
            Call<AsmItem> call = api.create(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(AsmItem entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(AsmItem entity) {
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
