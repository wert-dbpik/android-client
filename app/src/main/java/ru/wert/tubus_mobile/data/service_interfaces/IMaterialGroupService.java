package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.MaterialGroup;

public interface IMaterialGroupService extends ItemService<MaterialGroup> {

    MaterialGroup findByName(String name);

    MaterialGroup getRootItem();
}
