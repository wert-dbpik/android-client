package ru.wert.bazapik_mobile.data.service_interfaces;


import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.VersionServer;

public interface IVersionServerService extends ItemService<VersionServer> {

    VersionServer findByName(String name);

}
