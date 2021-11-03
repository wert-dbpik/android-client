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

public abstract class BaseActivity extends AppCompatActivity {
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public static DraftQuickService DRAFT_SERVICE;
    public static PassportQuickService PASSPORT_SERVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("DBPIKSettings", MODE_PRIVATE);
        editor = settings.edit();
    }


    protected Context getAppContext(){
        return this;
    }

    protected String getProp(String name){

        switch(name){
            case "BASE_IP":
                return settings.getString(name, "10.0.2.2");
            case "LAST_USER_NAME":
                return settings.getString(name, "");
            default:
                return "NotFoundProperty";
        }
    }

    @SuppressLint("CommitPrefEdits")
    protected void setProp(String name, String val){
        editor.putString(name, val);
        editor.apply();
    }

    protected void showToast(String text){
        Toast toast = Toast.makeText(BaseActivity.this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Загрузка в память обновленных данных из БД
     */
    protected void reloadDataFromDB(){
        DataLoader.LoadDataTask task = new DataLoader.LoadDataTask(BaseActivity.this);
        task.execute();
    }
}
