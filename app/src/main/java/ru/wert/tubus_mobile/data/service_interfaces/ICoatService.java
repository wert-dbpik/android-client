package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Coat;

public interface ICoatService extends ItemService<Coat> {

    Coat findByName(String name);

}
