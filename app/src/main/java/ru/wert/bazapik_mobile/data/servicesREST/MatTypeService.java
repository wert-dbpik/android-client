package ru.wert.bazapik_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.MatTypeApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.IMatTypeService;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.MatType;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;

public class MatTypeService implements IMatTypeService, ItemService<MatType> {

    private static MatTypeService instance;
    private MatTypeApiInterface api;

    private MatTypeService() {
        BLlinks.matTypeService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(MatTypeApiInterface.class);
    }

    public MatTypeApiInterface getApi() {
        return api;
    }

    public static MatTypeService getInstance() {
        if (instance == null)
            return new MatTypeService();
        return instance;
    }

    @Override
    public MatType findById(Long id) {
        try {
            Call<MatType> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public MatType findByName(String name) {
        try {
            Call<MatType> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MatType> findAll() {
        try {
            Call<List<MatType>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MatType> findAllByText(String text) {
        try {
            Call<List<MatType>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MatType save(MatType entity) {
        try {
            Call<MatType> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(MatType entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(MatType entity) {
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
