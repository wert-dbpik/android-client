package ru.wert.tubus_mobile.dataPreloading;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.constants.Consts;
import ru.wert.tubus_mobile.main.BaseActivity;

/**
 * В классе происходит подгрузка базы данных
 * При успешной загрузке приложение открывает окно Поиска, при неудачной - закрывается
 * Так же загрузка данных вызывается из меню окна Поиска для обновления данных
 */
public class DataLoadingActivity extends BaseActivity {
    private DataLoadingAsyncTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loading);

        // Проверяем, нужно ли принудительное обновление
        boolean forceRefresh = getIntent().getBooleanExtra("FORCE_REFRESH", false);

        new Thread(() -> {
            Consts.TEMP_DIR = DataLoadingActivity.this.getCacheDir();

            CacheManager cacheManager = new CacheManager(this);
            boolean hasCache = cacheManager.hasAnyCachedData();

            try {
                if (hasCache && !forceRefresh) {
                    currentTask = new DataLoadingAsyncTask(this, true);
                    currentTask.execute();
                } else {
                    if (forceRefresh) {
                        cacheManager.clearCache();
                    }
                    currentTask = new DataLoadingAsyncTask(this, false);
                    currentTask.execute();
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (cacheManager.hasAnyCachedData()) {
                        new AlertDialog.Builder(DataLoadingActivity.this)
                                .setTitle("Внимание!")
                                .setMessage("Не удалось загрузить свежие данные. Используем данные из кэша.")
                                .setPositiveButton("OK", (arg0, arg1) -> {
                                    new DataLoadingAsyncTask(DataLoadingActivity.this, true).execute();
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialog.Builder(DataLoadingActivity.this)
                                .setTitle("Внимание!")
                                .setMessage("Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!")
                                .setPositiveButton("OK", (arg0, arg1) -> exitApplication())
                                .setCancelable(false)
                                .show();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Отменяем задачу при уничтожении активности
        if (currentTask != null && currentTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentTask.cancel(true);

            // Очищаем временный кэш при прерывании
            CacheManager cacheManager = new CacheManager(this);
            cacheManager.clearTempCache();
        }
    }
}