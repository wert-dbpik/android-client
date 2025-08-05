package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.AnyPartType;

public interface IAnyPartTypeService extends ItemService<AnyPartType> {

    AnyPartType findByName(String name);

}
