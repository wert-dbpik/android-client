package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Material;

public interface IMaterialService extends ItemService<Material> {

    Material findByName(String name);

    Material createFakeProduct(String name);
}
