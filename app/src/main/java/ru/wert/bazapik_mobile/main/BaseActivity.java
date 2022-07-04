package ru.wert.bazapik_mobile.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.constants.StaticMethods;
import ru.wert.bazapik_mobile.data.api_interfaces.AppLogApiInterface;
import ru.wert.bazapik_mobile.data.models.AppLog;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoader;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingAsyncTask;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";


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
            new WarningDialog1().show(this, "Внимание", "Сервер не доступен, поробуйте позднее");
        } else {
            Log.d(TAG, "Проблемы на сервере");
            new WarningDialog1().show(this, "Внимание!", "Проблемы на сервере");
        }
    }

    protected void handleBadRequest(Response response, String TAG) {
        switch (response.code()) {
            case 404:
                Log.e(TAG, "Проблемы с доступом к серверу");
                new WarningDialog1().show(this, "Внимание", "Сервер не доступен, поробуйте позднее");
                break;
            case 500:
                Log.e(TAG, "Проблемы на серверу");
                new WarningDialog1().show(this, "Внимание", "Ошибка на сервере " + response.code());
                break;
        }
    }

    /**
     * Метод создает запись лога в базе данных
     * CURRENT_PROJECT_VERSION не определяется при запуске приложения из-под IDE
     */
    public void createLog(boolean forAdminOnly, String text) {

        if (forAdminOnly && !CURRENT_USER.isLogging()) return;
        AppLogApiInterface api = RetrofitClient.getInstance().getRetrofit().create(AppLogApiInterface.class);
        //LocalDateTime.now() не работает, поэтому используем Date
        Date date = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        AppLog log = new AppLog(
                df.format(date),
                forAdminOnly,
                CURRENT_USER,
                1,
                APPLICATION_VERSION,
                text
        );


        Call<AppLog> call = api.create(log);
//        try {
//            call.execute();
//        } catch (IOException e) {
//            Log.e(TAG, e.getMessage());
//        }
        call.enqueue(new Callback<AppLog>() {
            @Override
            public void onResponse(Call<AppLog> call, Response<AppLog> response) {
                if(!response.isSuccessful())
                    handleBadRequest(response, TAG);
            }

            @Override
            public void onFailure(Call<AppLog> call, Throwable t) {
                handleOnFailure(t, TAG);
            }
        });

    }

    /**
     * Загрузка в память обновленных данных из БД
     */
    protected void reloadDataFromDB(){
//        DataLoader.LoadDataTask task = new DataLoader.LoadDataTask(BaseActivity.this);
//        task.execute();

        AsyncTask<Void, Void, Void> task = new DataLoadingAsyncTask(this);
        task.execute();
    }

    protected void exitApplication(){
        createLog(true, "Вышел из приложения");
        StaticMethods.clearAppCash();

        Intent sweetHome = new Intent(Intent.ACTION_MAIN);
        sweetHome.addCategory(Intent.CATEGORY_HOME);
        startActivity(sweetHome);
        finishAndRemoveTask();
        System.exit(0);
    }

    protected String parseLDTtoDate(String localDateTime) {
       String time = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date d = df.parse(localDateTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);

            DateFormat newFD = new SimpleDateFormat("dd MMM yyyy");
            time = newFD.format(d);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

}
