package ru.wert.bazapik_mobile.data.service_interfaces;


import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.VersionAndroid;

public interface IVersionAndroidService extends ItemService<VersionAndroid> {

    VersionAndroid findByName(String name);

}
