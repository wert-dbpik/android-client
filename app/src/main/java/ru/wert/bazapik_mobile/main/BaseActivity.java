package ru.wert.bazapik_mobile.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.wert.bazapik_mobile.dataPreloading.DataLoader;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;

import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_FOLDERS;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Отображаем тост из любого потока
     * @param text String текст сообщения
     */
    protected void showToast(String text){
        runOnUiThread(()->{
            Toast toast = Toast.makeText(BaseActivity.this, text, Toast.LENGTH_LONG);
            toast.show();
        });

    }

    /**
     * Загрузка в память обновленных данных из БД
     */
    protected void reloadDataFromDB(){
        DataLoader.LoadDataTask task = new DataLoader.LoadDataTask(BaseActivity.this);
        task.execute();
    }


}
