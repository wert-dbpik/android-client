package ru.wert.tubus_mobile.dataPreloading;

import android.app.Activity;
import android.util.Log;

import java.util.stream.Collectors;

import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.servicesREST.DraftService;
import ru.wert.tubus_mobile.data.servicesREST.FileService;
import ru.wert.tubus_mobile.data.servicesREST.PassportService;

/**
 * Ускоренная загрузка - данные загружаются из локального кэша мгновенно
 *
 * Оффлайн-работа - приложение работает без интернета, используя кэшированные данные
 *
 * Фоновое обновление - свежие данные подгружаются в фоне после показа кэшированных
 *
 * Отказоустойчивость - при ошибках сети используются последние доступные данные
 *
 * Автоматическое управление кэшем - данные автоматически обновляются раз в 24 часа
 */
public class DataLoader {


    public void load(Activity activity) throws Exception{

        FileService.getInstance();

        //Создается PassportService, и затем PassportQuickService
        new PassportService(activity);
//        new PassportQuickService(activity);

        //Создается DraftService, и затем DraftQuickService
        new DraftService();
//        new DraftQuickService();

        DataLoadingAsyncTask task = new DataLoadingAsyncTask(activity);
        task.execute();
    }

    public void refreshData(Activity activity, boolean forceRefresh) throws Exception {
        CacheManager cacheManager = new CacheManager(activity);

        if (forceRefresh || !cacheManager.isCacheValid()) {
            // Загружаем свежие данные
            BatchResponse response = BatchService.loadInitialData();
            if (response != null) {
                cacheManager.saveDataToCache(response);
                processData(response);
            }
        } else {
            // Используем кэшированные данные
            BatchResponse cachedResponse = cacheManager.loadDataFromCache();
            if (cachedResponse != null) {
                processData(cachedResponse);
            }
        }
    }

    private void processData(BatchResponse response) {
        if (response == null) return;

        // Обрабатываем пользователей
        if (response.getUsers() != null) {
            ThisApplication.LIST_OF_ALL_USERS = response.getUsers().stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }

        // Обрабатываем комнаты/чаты
        if (response.getRooms() != null) {
            ThisApplication.LIST_OF_ALL_ROOMS = response.getRooms().stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }

        // Обрабатываем группы изделий
        if (response.getProductGroups() != null) {
            ThisApplication.LIST_OF_ALL_PRODUCT_GROUPS = response.getProductGroups().stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }

        // Обрабатываем чертежи
        if (response.getDrafts() != null) {
            ThisApplication.LIST_OF_ALL_DRAFTS = response.getDrafts().stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }

        // Обрабатываем комплекты/папки
        if (response.getFolders() != null) {
            ThisApplication.LIST_OF_ALL_FOLDERS = response.getFolders().stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }

        // Обрабатываем паспорта
        if (response.getPassports() != null) {
            ThisApplication.LIST_OF_ALL_PASSPORTS = response.getPassports().stream()
                    .sorted(ThisApplication.passportsComparatorDetailFirst())
                    .collect(Collectors.toList());
        }

        // Обновляем время последней синхронизации
        ThisApplication.LAST_SYNC_TIME = System.currentTimeMillis();

        // Уведомляем о завершении обработки данных
        Log.i("DataLoader", "Данные успешно обработаны: " +
                (response.getUsers() != null ? response.getUsers().size() + " users, " : "") +
                (response.getRooms() != null ? response.getRooms().size() + " rooms, " : "") +
                (response.getProductGroups() != null ? response.getProductGroups().size() + " productGroups, " : "") +
                (response.getDrafts() != null ? response.getDrafts().size() + " drafts, " : "") +
                (response.getFolders() != null ? response.getFolders().size() + " folders, " : "") +
                (response.getPassports() != null ? response.getPassports().size() + " passports" : ""));
    }

}
