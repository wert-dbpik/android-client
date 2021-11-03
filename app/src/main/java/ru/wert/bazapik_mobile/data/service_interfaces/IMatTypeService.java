package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.MatType;

public interface IMatTypeService extends ItemService<MatType> {

    MatType findByName(String name);

}
