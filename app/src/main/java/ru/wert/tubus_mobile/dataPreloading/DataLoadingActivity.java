package ru.wert.tubus_mobile.dataPreloading;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loading);

        new Thread(()->{
            //Получаем временную папку
            Consts.TEMP_DIR = DataLoadingActivity.this.getCacheDir();

            // Проверяем наличие кэша
            CacheManager cacheManager = new CacheManager(this);
            boolean hasCache = cacheManager.hasAnyCachedData();

            try {
                if (hasCache) {
                    // Используем асинхронную задачу с кэшированием
                    new DataLoadingAsyncTask(this).execute();
                } else {
                    // Если кэша нет, загружаем напрямую
                    new DataLoader().load(this);
                }

            } catch (Exception e) {
                runOnUiThread(()->{
                    // Пытаемся использовать кэш при ошибке
                    if (cacheManager.hasAnyCachedData()) {
                        new AlertDialog.Builder(DataLoadingActivity.this)
                                .setTitle("Внимание!")
                                .setMessage("Не удалось загрузить свежие данные. Используем данные из кэша.")
                                .setPositiveButton("OK", (arg0, arg1) -> {
                                    new DataLoadingAsyncTask(DataLoadingActivity.this).execute();
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
}