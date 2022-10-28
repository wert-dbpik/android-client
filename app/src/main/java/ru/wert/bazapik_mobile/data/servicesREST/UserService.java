package ru.wert.bazapik_mobile.data.servicesREST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.service_interfaces.IUserService;
import ru.wert.bazapik_mobile.data.util.BLlinks;
import ru.wert.bazapik_mobile.data.models.User;

public class UserService implements IUserService, ItemService<User> {

    private static UserService instance;
    private UserApiInterface api;

    private UserService() {
        BLlinks.userService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
    }

    public UserApiInterface getApi() {
        return api;
    }

    public static UserService getInstance() {
        if (instance == null)
            return new UserService();
        return instance;
    }

    @Override
    public User findById(Long id) {
        try {
            Call<User> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public User findByName(String name) {
        try {
            Call<User> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByPassword(String pass) {
        try {
            Call<User> call = api.getByPassword(pass);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        try {
            Call<List<User>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAllByText(String text) {
        try {
            Call<List<User>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User save(User entity) {
        try {
            Call<User> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(User entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(User entity) {
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
    public List<Room> subscribeRoom(User user, Room room) {
        try {
            Call<Set<Room>> call = api.subscribeRoom(user.getId(), room.getId());
            return new ArrayList<Room>(call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Room> unsubscribeRoom(User user, Room room) {
        try {
            Call<Set<Room>> call = api.unsubscribeRoom(user.getId(), room.getId());
            return new ArrayList<Room>(call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
