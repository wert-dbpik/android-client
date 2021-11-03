package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Coat;

public interface ICoatService extends ItemService<Coat> {

    Coat findByName(String name);

}
