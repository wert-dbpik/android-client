package ru.wert.bazapik_mobile.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Response;
import ru.wert.bazapik_mobile.LoginActivity;
import ru.wert.bazapik_mobile.constants.StaticMethods;
import ru.wert.bazapik_mobile.data.api_interfaces.AppLogApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.AppLog;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.servicesREST.AppLogService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoader;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.warnings.Warning1;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
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

    protected void handleOnFailure(Throwable t, String TAG){
        if(t.getMessage().contains("Failed to connect")) {
            Log.d(TAG, "Проблемы с доступом к серверу: " + t.getMessage());
            new Warning1().show(this, "Внимание", "Сервер не доступен, поробуйте позднее");
        } else {
            Log.d(TAG, "Проблемы на сервере");
            new Warning1().show(this, "Внимание!", "Проблемы на сервере");
        }
    }

    protected void handleBadRequest(Response response, String TAG) {
        switch (response.code()) {
            case 404:
                Log.d(TAG, "Проблемы с доступом к серверу");
                new Warning1().show(this, "Внимание", "Сервер не доступен, поробуйте позднее");
                break;
            case 500:
                new Warning1().show(this, "Внимание", "Ошибка на сервере " + response.code());
                break;
        }
    }

    /**
     * Метод создает запись лога в базе данных
     * CURRENT_PROJECT_VERSION не определяется при запуске приложения из-под IDE
     */
    protected void createLog(boolean forAdminOnly, String text) {
//        Time time = new Time();   time.setToNow();
        if (forAdminOnly && !CURRENT_USER.isLogging()) return;
        AppLogApiInterface api = RetrofitClient.getInstance().getRetrofit().create(AppLogApiInterface.class);
        api.create(new AppLog(
//                LocalDateTime.now().toString(),
                Calendar.getInstance().getTime().toString(),
                forAdminOnly,
                CURRENT_USER,
                1,
                APPLICATION_VERSION,
                text
        ));

    }

    /**
     * Загрузка в память обновленных данных из БД
     */
    protected void reloadDataFromDB(){
        DataLoader.LoadDataTask task = new DataLoader.LoadDataTask(BaseActivity.this);
        task.execute();
    }

    protected void exitApplication(){
        StaticMethods.clearAppCash();

        Intent sweetHome = new Intent(Intent.ACTION_MAIN);
        sweetHome.addCategory(Intent.CATEGORY_HOME);
        startActivity(sweetHome);
        finishAndRemoveTask();
        System.exit(0);
    }

}
