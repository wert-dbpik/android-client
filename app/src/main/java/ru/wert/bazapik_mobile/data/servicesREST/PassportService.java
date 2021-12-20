package ru.wert.bazapik_mobile.data.servicesREST;

import android.app.Activity;
import android.content.Context;

import retrofit2.Call;
import retrofit2.Response;
import ru.wert.bazapik_mobile.MainActivity;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Prefix;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.service_interfaces.IPassportService;
import ru.wert.bazapik_mobile.data.util.BLlinks;
import ru.wert.bazapik_mobile.data.api_interfaces.PassportApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.dataPreloading.DataLoader;
import ru.wert.bazapik_mobile.warnings.Warning1;


import java.io.IOException;
import java.util.List;

public class PassportService implements IPassportService, ItemService<Passport> {

    private PassportApiInterface api;
    private Context context;

    public PassportService(Context context) {
        this.context = context;
        ThisApplication.PASSPORT_SERVICE = this;
        api = RetrofitClient.getInstance().getRetrofit().create(PassportApiInterface.class);
    }

    public PassportApiInterface getApi() {
        return api;
    }

    @Override
    public Passport findById(Long id) {
        try {
            Call<Passport> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Passport> findAll() throws Exception{
//        try {
            Call<List<Passport>> call = api.getAll();
            Response<List<Passport>> response = call.execute();
            if(response.isSuccessful())
                return (response.body());
            else {
                new Warning1().show(context, "Внимание!","Проблемы на линии!");
            }
//        } catch (IOException e) {
//            throw new Exception();
//
//            new Warning1().show(context, "Внимание!","Проблемы на линии!");
//        }
        return null;
    }

    @Override
    public Passport findByPrefixIdAndNumber(Prefix prefix, String number) {
        try {
            Call<Passport> call = api.getAllByPrefixIdAndNumber(prefix.getId(), number);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Passport> findAllByName(String name) {
        try {
            Call<List<Passport>> call = api.getAllByName(name);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Passport> findAllByText(String text) {
        try {
            Call<List<Passport>> call = api.getAllByText(text);;
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(Passport entity) {
        try {
            Call<Passport> call = api.create(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Passport entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Passport entity) {
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
