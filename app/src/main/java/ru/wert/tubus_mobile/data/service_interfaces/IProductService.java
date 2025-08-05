package ru.wert.tubus_mobile.data.service_interfaces;

import java.util.List;
import java.util.Set;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Product;

public interface IProductService extends ItemService<Product> {

    Product findByPassportId(Long id);

    List<Product> findAllByFolderId(Long id);

    List<Product> findAllByProductGroupId(Long id);

    Set<Draft> findDrafts(Product product);

    Set<Draft> addDrafts(Product product, Draft draft);

    Set<Draft> removeDrafts(Product product, Draft draft);
}
