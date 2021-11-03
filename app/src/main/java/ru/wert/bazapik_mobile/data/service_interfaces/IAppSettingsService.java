package ru.wert.bazapik_mobile.data.service_interfaces;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AppSettings;

public interface IAppSettingsService extends ItemService<AppSettings> {

    AppSettings findByName(String name);

}
