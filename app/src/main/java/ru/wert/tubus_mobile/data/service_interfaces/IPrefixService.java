package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Prefix;

public interface IPrefixService extends ItemService<Prefix> {

    Prefix findByName(String name);

}
