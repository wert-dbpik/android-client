package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AnyPart;

public interface IAnyPartService extends ItemService<AnyPart> {

    AnyPart findByName(String name);

}
