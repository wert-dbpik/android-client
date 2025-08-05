package ru.wert.tubus_mobile.data.service_interfaces;


import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.VersionDesktop;

public interface IVersionDesktopService extends ItemService<VersionDesktop> {

    VersionDesktop findByName(String name);

}
