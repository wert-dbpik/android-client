package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Material;

public interface IMaterialService extends ItemService<Material> {

    Material findByName(String name);

    Material createFakeProduct(String name);
}
