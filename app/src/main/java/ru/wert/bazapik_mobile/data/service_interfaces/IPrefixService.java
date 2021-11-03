package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Prefix;

public interface IPrefixService extends ItemService<Prefix> {

    Prefix findByName(String name);

}
