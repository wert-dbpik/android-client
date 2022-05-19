package ru.wert.bazapik_mobile.data.service_interfaces;

import java.time.LocalDateTime;
import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.AppLog;
import ru.wert.bazapik_mobile.data.models.User;


public interface IAppLogService extends ItemService<AppLog> {

    AppLog findByName(String name);
    List<AppLog> findAllByTimeBetween(LocalDateTime startTime, LocalDateTime finishTime);
    List<AppLog> findAllByTimeBetweenAndAdminOnlyFalse(LocalDateTime startTime, LocalDateTime finishTime);
    List<AppLog> findAllByUser(User user);
    List<AppLog> findAllByUserAndAdminOnlyFalse(User user);
    List<AppLog> findAllByApplication(Integer app);
    List<AppLog> findAllByApplicationAndAdminOnlyFalse(Integer app);
    List<AppLog> findAllByAdminOnlyFalse();

}
