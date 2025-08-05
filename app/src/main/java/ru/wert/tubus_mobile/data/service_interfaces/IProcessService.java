package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.TechProcess;

public interface IProcessService extends ItemService<TechProcess> {

    TechProcess findByName(String name);

}
