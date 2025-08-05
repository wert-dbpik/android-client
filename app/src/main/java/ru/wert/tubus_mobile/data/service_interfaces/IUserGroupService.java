package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.UserGroup;

public interface IUserGroupService extends ItemService<UserGroup> {

    UserGroup findByName(String name);

}
