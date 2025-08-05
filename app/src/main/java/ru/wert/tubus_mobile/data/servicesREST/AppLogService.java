package ru.wert.tubus_mobile.data.servicesREST;

import android.util.Log;

import retrofit2.Call;
import ru.wert.tubus_mobile.data.api_interfaces.AppLogApiInterface;
import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.AppLog;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.service_interfaces.IAppLogService;
import ru.wert.tubus_mobile.data.util.BLlinks;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AppLogService implements IAppLogService, ItemService<AppLog> {
    private static final String TAG = "+++ AppLogService +++";

    private static AppLogService instance;
    private AppLogApiInterface api;

    private AppLogService() {
        BLlinks.appLogService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(AppLogApiInterface.class);
    }

    public AppLogApiInterface getApi() {
        return api;
    }

    public static AppLogService getInstance() {
        if (instance == null)
            return new AppLogService();
        return instance;
    }

    @Override
    public AppLog findById(Long id) {
        try {
            Call<AppLog> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AppLog> findAllByUser(User user) {
        try {
            Call<List<AppLog>> call = api.getAllByUserId(user.getId());
            return call.execute().body();
        } catch (IOException e) {
            Log.e(TAG, "findAllByUser does wrong");
        }
        return null;
    }

    public List<AppLog> findAllByUserAndAdminOnlyFalse(User user) {
        try {
            Call<List<AppLog>> call = api.getAllByUserIdAndAdminOnlyFalse(user.getId());
            return call.execute().body();
        } catch (IOException e) {
            Log.e(TAG, "findAllByUserAndAdminOnlyFalse does wrong");
        }
        return null;
    }

    public List<AppLog> findAllByApplication(Integer app) {
        try {
            Call<List<AppLog>> call = api.getAllByApplication(app);
            return call.execute().body();
        } catch (IOException e) {
            Log.e(TAG, "findAllByApplication does wrong");
        }
        return null;
    }

    public List<AppLog> findAllByApplicationAndAdminOnlyFalse(Integer app) {
        try {
            Call<List<AppLog>> call = api.getAllByApplicationAndAdminOnlyFalse(app);
            return call.execute().body();
        } catch (IOException e) {
            Log.e(TAG, "findAllByApplicationAndAdminOnlyFalse does wrong");
        }
        return null;
    }

    @Override
    public List<AppLog> findAllByTimeBetween(LocalDateTime startTime, LocalDateTime finishTime) {
        try {
            Call<List<AppLog>> call = api.getAllByTimeBetween(startTime, finishTime);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AppLog> findAllByTimeBetweenAndAdminOnlyFalse(LocalDateTime startTime, LocalDateTime finishTime) {
        try {
            Call<List<AppLog>> call = api.getAllByTimeBetweenAndAdminOnlyFalse(startTime, finishTime);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<AppLog> findAll() {
        try {
            Call<List<AppLog>> call = api.getAll();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AppLog> findAllByAdminOnlyFalse() {
        try {
            Call<List<AppLog>> call = api.getAllAdminOnlyFalse();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AppLog> findAllByText(String text) {
        //НЕ ИСПОЛЬЗУЕТСЯ
        return null;
    }


    @Override
    public AppLog save(AppLog entity) {
        try {
            Call<AppLog> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(AppLog appLog) {
        // НЕ ИСПОЛЬЗУЕТСЯ
        return false;
    }


    @Override
    public boolean delete(AppLog entity) {
        Long id = entity.getId();
        try {
            Call<Void> call = api.deleteById(id);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AppLog findByName(String name) {
        return null;
    }

}
