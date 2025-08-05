package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.AnyPart;

public interface IAnyPartService extends ItemService<AnyPart> {

    AnyPart findByName(String name);

}
