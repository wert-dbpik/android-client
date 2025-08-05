package ru.wert.tubus_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Folder;

public interface IFolderService extends ItemService<Folder> {

    Folder findByName(String name);

    List<Folder> findAllByGroupId(Long id);

}
