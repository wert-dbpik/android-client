package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Density;

public interface IDensityService extends ItemService<Density> {

    Density findByName(String name);

    Density findByValue(double value);

}
