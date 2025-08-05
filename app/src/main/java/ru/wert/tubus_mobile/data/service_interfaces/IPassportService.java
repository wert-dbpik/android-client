package ru.wert.tubus_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.models.Passport;
import ru.wert.tubus_mobile.data.models.Prefix;

public interface IPassportService extends ItemService<Passport> {

    Passport findByPrefixIdAndNumber(Prefix prefix, String number);

    List<Passport> findAllByName(String name);

}
