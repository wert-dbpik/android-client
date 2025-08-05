package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.MatType;

public interface IMatTypeService extends ItemService<MatType> {

    MatType findByName(String name);

}
