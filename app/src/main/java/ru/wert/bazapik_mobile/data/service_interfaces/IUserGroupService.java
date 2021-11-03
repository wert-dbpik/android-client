package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.UserGroup;

public interface IUserGroupService extends ItemService<UserGroup> {

    UserGroup findByName(String name);

}
