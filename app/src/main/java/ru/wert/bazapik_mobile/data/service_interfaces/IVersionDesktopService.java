package ru.wert.bazapik_mobile.data.service_interfaces;


import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.VersionDesktop;

public interface IVersionDesktopService extends ItemService<VersionDesktop> {

    VersionDesktop findByName(String name);

}
