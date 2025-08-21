package ru.wert.tubus_mobile.dataPreloading;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.data.models.Passport;
import ru.wert.tubus_mobile.data.models.ProductGroup;
import ru.wert.tubus_mobile.data.models.Room;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.dataPreloading.BatchResponse;

/**
 * МЕНЕДЖЕР КЭШИРОВАНИЯ ДАННЫХ
 * Управляет сохранением и загрузкой данных в локальное хранилище
 */
public class CacheManager {
    private static final String TAG = "CacheManager";
    private static final String CACHE_DIR = "app_cache";
    private static final long CACHE_VALIDITY_MS = TimeUnit.HOURS.toMillis(24); // 24 часа

    private final Context context;
    private final Gson gson;

    // ИМЕНА ФАЙЛОВ ДЛЯ РАЗНЫХ ТИПОВ ДАННЫХ
    private static final String FILE_USERS = "users.json";
    private static final String FILE_ROOMS = "rooms.json";
    private static final String FILE_PRODUCT_GROUPS = "product_groups.json";
    private static final String FILE_DRAFTS = "drafts.json";
    private static final String FILE_FOLDERS = "folders.json";
    private static final String FILE_PASSPORTS = "passports.json";
    private static final String FILE_METADATA = "cache_metadata.json";

    public CacheManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    /**
     * СОХРАНЕНИЕ ДАННЫХ В КЭШ
     */
    public void saveDataToCache(BatchResponse response) {
        try {
            // СОЗДАЕМ ДИРЕКТОРИЮ ДЛЯ КЭША, ЕСЛИ ЕЁ НЕТ
            File cacheDir = getCacheDirectory();
            if (!cacheDir.exists()) {
                boolean created = cacheDir.mkdirs();
                if (!created) {
                    Log.e(TAG, "Не удалось создать директорию кэша");
                    return;
                }
            }

            // СОХРАНЯЕМ КАЖДЫЙ ТИП ДАННЫХ В ОТДЕЛЬНЫЙ ФАЙЛ
            saveListToFile(FILE_USERS, response.getUsers());
            saveListToFile(FILE_ROOMS, response.getRooms());
            saveListToFile(FILE_PRODUCT_GROUPS, response.getProductGroups());
            saveListToFile(FILE_DRAFTS, response.getDrafts());
            saveListToFile(FILE_FOLDERS, response.getFolders());
            saveListToFile(FILE_PASSPORTS, response.getPassports());

            // СОХРАНЯЕМ МЕТАДАННЫЕ (ВРЕМЯ СОЗДАНИЯ КЭША)
            CacheMetadata metadata = new CacheMetadata(new Date());
            saveToFile(FILE_METADATA, metadata);

            Log.i(TAG, "Данные успешно сохранены в кэш");

        } catch (Exception e) {
            Log.e(TAG, "Ошибка сохранения данных в кэш: " + e.getMessage());
        }
    }

    /**
     * ЗАГРУЗКА ДАННЫХ ИЗ КЭША
     */
    public BatchResponse loadDataFromCache() {
        try {
            // ПРОВЕРЯЕМ АКТУАЛЬНОСТЬ КЭША
            if (!isCacheValid()) {
                Log.i(TAG, "Кэш устарел или отсутствует");
                return null;
            }

            BatchResponse response = new BatchResponse();

            // ЗАГРУЗКА КАЖДОГО ТИПА ДАННЫХ ИЗ ФАЙЛОВ
            response.setUsers(loadListFromFile(FILE_USERS, new TypeToken<List<User>>(){}.getType()));
            response.setRooms(loadListFromFile(FILE_ROOMS, new TypeToken<List<Room>>(){}.getType()));
            response.setProductGroups(loadListFromFile(FILE_PRODUCT_GROUPS, new TypeToken<List<ProductGroup>>(){}.getType()));
            response.setDrafts(loadListFromFile(FILE_DRAFTS, new TypeToken<List<Draft>>(){}.getType()));
            response.setFolders(loadListFromFile(FILE_FOLDERS, new TypeToken<List<Folder>>(){}.getType()));
            response.setPassports(loadListFromFile(FILE_PASSPORTS, new TypeToken<List<Passport>>(){}.getType()));

            // ПРОВЕРЯЕМ, ЧТО ВСЕ ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ
            if (response.getUsers() != null && response.getRooms() != null &&
                    response.getProductGroups() != null && response.getDrafts() != null &&
                    response.getFolders() != null && response.getPassports() != null) {

                Log.i(TAG, "Данные успешно загружены из кэша");
                return response;
            } else {
                Log.w(TAG, "Не все данные загружены из кэша");
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных из кэша: " + e.getMessage());
            return null;
        }
    }

    /**
     * ЗАГРУЗКА ДАННЫХ ИЗ КЭША (ДАЖЕ УСТАРЕВШИХ)
     * Используется при отсутствии интернета
     */
    public BatchResponse loadDataFromCacheForce() {
        try {
            BatchResponse response = new BatchResponse();

            response.setUsers(loadListFromFile(FILE_USERS, new TypeToken<List<User>>(){}.getType()));
            response.setRooms(loadListFromFile(FILE_ROOMS, new TypeToken<List<Room>>(){}.getType()));
            response.setProductGroups(loadListFromFile(FILE_PRODUCT_GROUPS, new TypeToken<List<ProductGroup>>(){}.getType()));
            response.setDrafts(loadListFromFile(FILE_DRAFTS, new TypeToken<List<Draft>>(){}.getType()));
            response.setFolders(loadListFromFile(FILE_FOLDERS, new TypeToken<List<Folder>>(){}.getType()));
            response.setPassports(loadListFromFile(FILE_PASSPORTS, new TypeToken<List<Passport>>(){}.getType()));

            // ПРОВЕРЯЕМ, ЧТО ХОТЯ БЫ ЧАСТЬ ДАННЫХ ЗАГРУЖЕНА
            if (response.getUsers() != null || response.getRooms() != null ||
                    response.getProductGroups() != null || response.getDrafts() != null ||
                    response.getFolders() != null || response.getPassports() != null) {

                Log.i(TAG, "Данные загружены из кэша (принудительно)");
                return response;
            } else {
                Log.w(TAG, "Нет данных в кэше для принудительной загрузки");
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка принудительной загрузки из кэша: " + e.getMessage());
            return null;
        }
    }

    /**
     * ПРОВЕРКА АКТУАЛЬНОСТИ КЭША
     */
    public boolean isCacheValid() {
        try {
            CacheMetadata metadata = loadFromFile(FILE_METADATA, CacheMetadata.class);
            if (metadata == null) {
                return false;
            }

            long cacheAge = System.currentTimeMillis() - metadata.getCreatedAt().getTime();
            return cacheAge < CACHE_VALIDITY_MS;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ПРОВЕРКА НАЛИЧИЯ ЛЮБЫХ ДАННЫХ В КЭШЕ
     */
    public boolean hasAnyCachedData() {
        File cacheDir = getCacheDirectory();
        if (!cacheDir.exists()) return false;

        File[] files = cacheDir.listFiles();
        return files != null && files.length > 1; // больше 1 потому что есть metadata файл
    }

    /**
     * ОЧИСТКА КЭША
     */
    public void clearCache() {
        try {
            File cacheDir = getCacheDirectory();
            if (cacheDir.exists()) {
                deleteDirectory(cacheDir);
                Log.i(TAG, "Кэш успешно очищен");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка очистки кэша: " + e.getMessage());
        }
    }

    /**
     * ПОЛУЧЕНИЕ РАЗМЕРА КЭША
     */
    public long getCacheSize() {
        File cacheDir = getCacheDirectory();
        return getDirectorySize(cacheDir);
    }

    /**
     * ПОЛУЧЕНИЕ ВРЕМЕНИ ПОСЛЕДНЕГО ОБНОВЛЕНИЯ КЭША
     */
    public Date getLastUpdateTime() {
        try {
            CacheMetadata metadata = loadFromFile(FILE_METADATA, CacheMetadata.class);
            return metadata != null ? metadata.getCreatedAt() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    private <T> void saveListToFile(String filename, List<T> list) {
        saveToFile(filename, list);
    }

    private <T> void saveToFile(String filename, T data) {
        try {
            File file = new File(getCacheDirectory(), filename);
            String json = gson.toJson(data);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка сохранения в файл " + filename + ": " + e.getMessage());
        }
    }

    private <T> List<T> loadListFromFile(String filename, Type type) {
        try {
            File file = new File(getCacheDirectory(), filename);
            if (!file.exists()) return null;

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                String json = new String(data, "UTF-8");
                return gson.fromJson(json, type);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки из файл " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private <T> T loadFromFile(String filename, Class<T> clazz) {
        try {
            File file = new File(getCacheDirectory(), filename);
            if (!file.exists()) return null;

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                String json = new String(data, "UTF-8");
                return gson.fromJson(json, clazz);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки из файл " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private File getCacheDirectory() {
        return new File(context.getFilesDir(), CACHE_DIR);
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getDirectorySize(file);
                }
            }
        } else if (directory.isFile()) {
            size = directory.length();
        }
        return size;
    }

    /**
     * КЛАСС ДЛЯ МЕТАДАННЫХ КЭША
     */
    private static class CacheMetadata {
        private Date createdAt;

        public CacheMetadata() {}

        public CacheMetadata(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
    }
}