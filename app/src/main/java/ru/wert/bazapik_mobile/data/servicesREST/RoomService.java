package ru.wert.bazapik_mobile.data.servicesREST;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.RoomApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.service_interfaces.IRoomService;
import ru.wert.bazapik_mobile.data.util.BLlinks;

public class RoomService  implements IRoomService, ItemService<Room> {
    private static RoomService instance;
    private RoomApiInterface api;

    private RoomService() {
//        BLlinks.roomService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(RoomApiInterface.class);
    }

    public RoomApiInterface getApi() {
        return api;
    }

    public static RoomService getInstance() {
        if (instance == null)
            return new RoomService();
        return instance;
    }

    @Override
    public Room findById(Long id) {
        try {
            Call<Room> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Room findByName(String name) {
        try {
            Call<Room> call = api.getByName(name);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Room addRoommates(List<String> userIds, Room room) {
        try {
            Call<Room> call = api.addRoommates(userIds, room.getId());
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Room removeRoommates(List<String> userIds, Room room) {
        try {
            Call<Room> call = api.removeRoommates(userIds, room.getId());
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Room> findAll() {
        try {
            Call<List<Room>> call = api.getAll();
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Room> findAllByText(String text) {
        //НЕ ИСПОЛЬЗУЕТСЯ
        return null;
    }

    @Override
    public Room save(Room entity) {
        try {
            Call<Room> call = api.create(entity);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean update(Room entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Room entity) {
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
