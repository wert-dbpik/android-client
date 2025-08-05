package ru.wert.tubus_mobile.data.api_interfaces;

import retrofit2.Call;
import retrofit2.http.*;
import ru.wert.tubus_mobile.data.models.Room;

import java.util.List;

public interface RoomApiInterface {

    @GET("rooms/id/{id}")
    Call<Room> getById(@Path("id") Long id);

    @GET("rooms/name/{name}")
    Call<Room> getByName(@Path("name") String name);

    @GET("rooms/all")
    Call<List<Room>> getAll();

    @POST("rooms/create")
    Call<Room> create(@Body Room entity);

    @PUT("rooms/update")
    Call<Void> update(@Body Room entity);

    @DELETE("rooms/delete/{id}")
    Call<Void> deleteById(@Path("id") Long id);

    @PUT("users/add-roommate/{roomId}")
    Call<Room> addRoommates(@Body List<String> userIds, @Path("roomId") Long roomId);

    @PUT("users/remove-roommate/{roomId}")
    Call<Room> removeRoommates(@Body List<String> userIds, @Path("roomId") Long roomId);

}
