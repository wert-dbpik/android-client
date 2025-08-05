package ru.wert.tubus_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.tubus_mobile.data.api_interfaces.ProcessApiInterface;
import ru.wert.tubus_mobile.data.service_interfaces.IProcessService;
import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.TechProcess;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.util.BLlinks;

import java.io.IOException;
import java.util.List;


public class ProcessService implements IProcessService, ItemService<TechProcess> {

    private static ProcessService instance;
    private ProcessApiInterface api;

    private ProcessService() {
        BLlinks.processService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(ProcessApiInterface.class);
    }

    public ProcessApiInterface getApi() {
        return api;
    }

    public static ProcessService getInstance() {
        if (instance == null)
            return new ProcessService();
        return instance;
    }

    @Override
    public TechProcess findById(Long id) {
        try {
            Call<TechProcess> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public TechProcess findByName(String name) {
        try {
            Call<TechProcess> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TechProcess> findAll() {
        try {
            Call<List<TechProcess>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TechProcess> findAllByText(String text) {
        try {
            Call<List<TechProcess>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TechProcess save(TechProcess entity) {
        try {
            Call<TechProcess> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(TechProcess entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(TechProcess entity) {
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
