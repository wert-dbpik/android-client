package ru.wert.tubus_mobile.data.service_interfaces;


import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.AsmItem;

public interface IAsmItemService extends ItemService<AsmItem> {

    AsmItem findByName(String name);

}
