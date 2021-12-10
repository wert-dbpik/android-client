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
    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    public static DraftQuickService DRAFT_SERVICE;
    public static PassportQuickService PASSPORT_SERVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("DBPIKSettings", MODE_PRIVATE);
        editor = settings.edit();
    }


//    protected Context getAppContext(){
//        return this;
//    }

    public static String getProp(String name){

        switch(name){
            case "IP":
                return settings.getString(name, "10.0.2.2");
            case "PORT":
                return settings.getString(name, "8080");
            case "USER_NAME":
                return settings.getString(name, "");
            case "SHOW_FOLDERS":
                return settings.getString(name, "false");
            case "HIDE_PREFIXES":
                return settings.getString(name, "false");
            default:
                return "NotFoundProperty";
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setProp(String name, String val){
        editor.putString(name, val);
        editor.apply();
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

    protected void loadSettings() {

        SHOW_FOLDERS = Boolean.parseBoolean(getProp("SHOW_FOLDERS"));
        HIDE_PREFIXES = Boolean.parseBoolean(getProp("HIDE_PREFIXES"));

    }
}
