package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.ProductGroup;

public interface IProductGroupService extends ItemService<ProductGroup> {

    ProductGroup findByName(String name);

    ProductGroup getRootItem();
}
