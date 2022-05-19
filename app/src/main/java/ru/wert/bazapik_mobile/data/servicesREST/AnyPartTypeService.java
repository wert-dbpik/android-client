package ru.wert.bazapik_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.AnyPartTypeApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.IAnyPartTypeService;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AnyPartType;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;


import java.io.IOException;
import java.util.List;

public class AnyPartTypeService implements IAnyPartTypeService, ItemService<AnyPartType> {

    private static AnyPartTypeService instance;
    private AnyPartTypeApiInterface api;

    private AnyPartTypeService() {
        BLlinks.partTypeService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(AnyPartTypeApiInterface.class);
    }

    public AnyPartTypeApiInterface getApi() {
        return api;
    }

    public static AnyPartTypeService getInstance() {
        if (instance == null)
            return new AnyPartTypeService();
        return instance;
    }

    @Override
    public AnyPartType findById(Long id) {
        try {
            Call<AnyPartType> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public AnyPartType findByName(String name) {
        try {
            Call<AnyPartType> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AnyPartType> findAll() {
        try {
            Call<List<AnyPartType>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AnyPartType> findAllByText(String text) {
        try {
            Call<List<AnyPartType>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AnyPartType save(AnyPartType entity) {
        try {
            Call<AnyPartType> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(AnyPartType entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(AnyPartType entity) {
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
