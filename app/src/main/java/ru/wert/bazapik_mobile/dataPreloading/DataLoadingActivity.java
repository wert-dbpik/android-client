package ru.wert.bazapik_mobile.dataPreloading;

import android.content.Intent;
import android.os.Bundle;

import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.search.SearchActivity;

public class DataLoadingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loading);

        new Thread(()->{
            //Получаем временную папку
            Consts.TEMP_DIR = DataLoadingActivity.this.getCacheDir(); // context being the Activity pointer
            new DataLoader().onCreate();
            runOnUiThread(()->{
                Intent intent = new Intent(DataLoadingActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }).start();
    }
}