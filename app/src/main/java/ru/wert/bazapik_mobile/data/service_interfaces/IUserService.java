package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.User;

public interface IUserService extends ItemService<User> {

    User findByName(String name);

    User findByPassword(String pass);

}
