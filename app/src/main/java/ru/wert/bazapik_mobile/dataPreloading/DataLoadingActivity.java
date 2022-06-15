package ru.wert.bazapik_mobile.dataPreloading;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.search.SearchActivity;

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
            Consts.TEMP_DIR = DataLoadingActivity.this.getCacheDir(); // context being the Activity pointer
            try {
                new DataLoader().load(this);
                runOnUiThread(()->{
                    Intent intent = new Intent(DataLoadingActivity.this, SearchActivity.class);
                    startActivity(intent);
                });
            } catch (Exception e) {
                runOnUiThread(()->{
                    new AlertDialog.Builder(DataLoadingActivity.this)
                            .setTitle("Внимание!")
                            .setMessage("Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!")
                            .setPositiveButton(android.R.string.yes, (arg0, arg1) -> exitApplication()).create().show();
                });

            }

        }).start();
    }
}