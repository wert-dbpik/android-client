package ru.wert.bazapik_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.models.User;

public interface IUserService extends ItemService<User> {

    User findByName(String name);

    User findByPassword(String pass);

    List<Room> subscribeRoom(User user, Room room);

    List<Room> unsubscribeRoom(User user, Room room);

}
