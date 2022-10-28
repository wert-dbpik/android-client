package ru.wert.bazapik_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Room;

public interface IRoomService extends ItemService<Room> {
    Room findByName(String name);
    Room addRoommates(List<String> userIds, Room room);
    Room removeRoommates(List<String> userIds, Room room);
}
