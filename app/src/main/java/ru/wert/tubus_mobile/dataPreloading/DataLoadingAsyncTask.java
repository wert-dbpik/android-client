package ru.wert.tubus_mobile.dataPreloading;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.models.*;
import ru.wert.tubus_mobile.organizer.OrganizerActivity;

public class DataLoadingAsyncTask extends AsyncTask<Void, String, Boolean> {
    private static final String TAG = "DataLoadingTask";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final Activity activity;
    private final TextView tvLoadingStatus;
    private int retryCount = 0;
    private final CacheManager cacheManager;
    private boolean useCache = true;

    public DataLoadingAsyncTask(Activity activity) {
        this.activity = activity;
        this.tvLoadingStatus = activity.findViewById(R.id.tvLoadingStatus);
        this.cacheManager = new CacheManager(activity);
    }

    public DataLoadingAsyncTask(Activity activity, boolean useCache) {
        this(activity);
        this.useCache = useCache;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.runOnUiThread(() -> {
            if (tvLoadingStatus != null) {
                tvLoadingStatus.setText("Подготовка к загрузке...");
            }
        });
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            // Пытаемся загрузить из кэша, если доступен и разрешено использование кэша
            if (useCache && cacheManager.isCacheValid()) {
                publishProgress("Загрузка из кэша...");
                BatchResponse cachedResponse = cacheManager.loadDataFromCache();
                if (cachedResponse != null) {
                    processResponseWithProgress(cachedResponse);
                    publishProgress("Данные загружены из кэша");

                    // Загружаем свежие данные в фоне для обновления кэша
                    new Thread(this::loadFreshDataInBackgroundWithTempCache).start();

                    return true;
                }
            }

            // Если кэш недоступен или использование кэша запрещено, загружаем с сервера
            return attemptDataLoadingWithTempCache();

        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка во время загрузки данных", e);
            publishProgress("Критическая ошибка");

            // Пытаемся загрузить устаревшие данные при ошибке
            if (cacheManager.hasAnyCachedData()) {
                publishProgress("Используем устаревшие данные...");
                BatchResponse forcedResponse = cacheManager.loadDataFromCacheForce();
                if (forcedResponse != null) {
                    processResponseWithProgress(forcedResponse);
                    return true;
                }
            }

            return false;
        }
    }

    private Boolean attemptDataLoadingWithTempCache() throws IOException {
        try {
            publishProgress("Подключаемся к серверу...");
            BatchResponse response = BatchService.loadInitialData();

            if (response == null) {
                throw new IOException("Пустой ответ от сервера");
            }

            // Обрабатываем данные для немедленного использования
            processResponseWithProgress(response);

            // Сохраняем данные во ВРЕМЕННЫЙ кэш
            publishProgress("Сохранение во временный кэш...");
            cacheManager.saveDataToTempCache(response);

            // АТОМАРНАЯ ОПЕРАЦИЯ: переносим временный кэш в основной
            publishProgress("Перенос данных в основной кэш...");
            boolean commitSuccess = cacheManager.commitTempCache();

            if (!commitSuccess) {
                Log.w(TAG, "Не удалось перенести временный кэш в основной, используем fallback...");
                // Fallback: сохраняем напрямую в основной кэш
                cacheManager.saveDataToCache(response);
            }

            publishProgress("Данные успешно загружены и сохранены");
            return true;

        } catch (IOException e) {
            // Очищаем временный кэш при ошибке
            cacheManager.clearTempCache();

            if (retryCount < MAX_RETRIES) {
                retryCount++;
                Log.w(TAG, "Попытка " + retryCount + " не удалась, повторяем...", e);
                publishProgress("Повторная попытка " + retryCount + "/" + MAX_RETRIES);

                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }

                return attemptDataLoadingWithTempCache();
            }

            // При окончательной ошибке пробуем загрузить из кэша
            if (cacheManager.hasAnyCachedData()) {
                publishProgress("Используем данные из кэша после ошибки...");
                BatchResponse forcedResponse = cacheManager.loadDataFromCacheForce();
                if (forcedResponse != null) {
                    processResponseWithProgress(forcedResponse);
                    return true;
                }
            }

            throw e;
        }
    }

    private void loadFreshDataInBackgroundWithTempCache() {
        try {
            Log.i(TAG, "Фоновая загрузка свежих данных для обновления кэша...");
            BatchResponse freshResponse = BatchService.loadInitialData();

            if (freshResponse != null) {
                // Сохраняем во временный кэш
                cacheManager.saveDataToTempCache(freshResponse);

                // Атомарно переносим в основной кэш
                boolean success = cacheManager.commitTempCache();

                if (success) {
                    Log.i(TAG, "Данные успешно обновлены в фоне");
                } else {
                    Log.w(TAG, "Не удалось обновить кэш в фоне");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Не удалось обновить данные в фоне: " + e.getMessage());
            // Очищаем временный кэш при ошибке
            cacheManager.clearTempCache();
        }
    }

    private void processResponseWithProgress(BatchResponse response) {
        String[] steps = {
                "пользователей", "чаты", "группы изделий",
                "чертежи", "комплекты", "паспорта"
        };

        Runnable[] processors = {
                () -> processUsers(response.getUsers()),
                () -> processRooms(response.getRooms()),
                () -> processProductGroups(response.getProductGroups()),
                () -> processDrafts(response.getDrafts()),
                () -> processFolders(response.getFolders()),
                () -> processPassports(response.getPassports())
        };

        for (int i = 0; i < steps.length; i++) {
            if (isCancelled()) {
                Log.i(TAG, "Загрузка данных прервана пользователем");
                return;
            }

            publishProgress("Обрабатываем " + steps[i] + "...");
            processors[i].run();

            // Небольшая задержка для плавного отображения прогресса
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (tvLoadingStatus != null && values.length > 0) {
            tvLoadingStatus.setText(values[0]);
        }
    }

    private void processUsers(List<User> users) {
        if (users != null) {
            ThisApplication.LIST_OF_ALL_USERS = users.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processRooms(List<Room> rooms) {
        if (rooms != null) {
            ThisApplication.LIST_OF_ALL_ROOMS = rooms.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processProductGroups(List<ProductGroup> productGroups) {
        if (productGroups != null) {
            ThisApplication.LIST_OF_ALL_PRODUCT_GROUPS = productGroups.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processDrafts(List<Draft> drafts) {
        if (drafts != null) {
            ThisApplication.LIST_OF_ALL_DRAFTS = drafts.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processFolders(List<Folder> folders) {
        if (folders != null) {
            ThisApplication.LIST_OF_ALL_FOLDERS = folders.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processPassports(List<Passport> passports) {
        if (passports != null) {
            ThisApplication.LIST_OF_ALL_PASSPORTS = passports.stream()
                    .filter(passport -> passport.getDraftIds() != null && !passport.getDraftIds().isEmpty()) // Фильтруем паспорта без чертежей
                    .sorted(ThisApplication.passportsComparatorDetailFirst())
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            startOrganizerActivity();
        } else {
            showRetryDialog();
        }
    }

    private void startOrganizerActivity() {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(activity, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    private void showRetryDialog() {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle("Ошибка загрузки")
                .setMessage("Не удалось загрузить данные. Проверьте подключение к интернету.")
                .setPositiveButton("Повторить", (d, w) -> retryLoading())
                .setNegativeButton("Использовать оффлайн", (d, w) -> useOfflineData())
                .setCancelable(false)
                .show());
    }

    private void useOfflineData() {
        if (cacheManager.hasAnyCachedData()) {
            publishProgress("Загрузка оффлайн данных...");
            BatchResponse offlineResponse = cacheManager.loadDataFromCacheForce();
            if (offlineResponse != null) {
                processResponseWithProgress(offlineResponse);
                startOrganizerActivity();
            } else {
                showRetryDialog();
            }
        } else {
            showRetryDialog();
        }
    }

    private void retryLoading() {
        new DataLoadingAsyncTask(activity, false).execute(); // При повторной попытке не используем кэш
    }
}