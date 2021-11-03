package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.TechProcess;

public interface IProcessService extends ItemService<TechProcess> {

    TechProcess findByName(String name);

}
