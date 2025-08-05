package ru.wert.tubus_mobile.data.service_interfaces;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.AppSettings;

public interface IAppSettingsService extends ItemService<AppSettings> {

    AppSettings findByName(String name);

}
