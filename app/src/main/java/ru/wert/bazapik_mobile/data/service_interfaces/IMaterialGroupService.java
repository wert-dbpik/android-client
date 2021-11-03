package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.MaterialGroup;

public interface IMaterialGroupService extends ItemService<MaterialGroup> {

    MaterialGroup findByName(String name);

    MaterialGroup getRootItem();
}
