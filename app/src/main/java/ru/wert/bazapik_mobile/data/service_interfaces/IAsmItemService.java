package ru.wert.bazapik_mobile.data.service_interfaces;


import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AsmItem;

public interface IAsmItemService extends ItemService<AsmItem> {

    AsmItem findByName(String name);

}
