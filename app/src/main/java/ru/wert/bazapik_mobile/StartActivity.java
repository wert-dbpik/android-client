package ru.wert.bazapik_mobile;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.REQUEST_CODE_PERMISSION_CAMERA;
import static ru.wert.bazapik_mobile.ThisApplication.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.constants.Consts.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.List;

import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

/**
 * Отправная точка работы приложения
 * 1) Запускается Ретрофит
 * 2) Осуществляется проверка доступности базы данных через загрузку пользователя с id = 1
 * 3) Если база доступна, то в таблице логов производится запись о поключении к серверу пользователя,
 *    указанного в файле Properties приложения и открывается Окно загрузки данных,
 *    иначе - открывается Окно подключения к серверу
 * 4) Загружаются настройки приложения
 */
public class StartActivity extends BaseActivity {

    private static final String TAG = "StartActivity";
    private String ip;
    private String baseUrl;
    private boolean logoTapped;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);


        Log.i(TAG, "onCreate: IP = " + getProp("IP"));
        Log.i(TAG, "onCreate: PORT = " + getProp("PORT"));
        Log.i(TAG, "onCreate: SHOW_SOLID_FILES = " + getProp("SHOW_SOLID_FILES"));
        Log.i(TAG, "onCreate: HIDE_PREFIXES = " + getProp("HIDE_PREFIXES"));

        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.setOnClickListener(v -> {
            logoTapped = true;
            startRetrofit();
            ThisApplication.loadSettings();
        });

        new Thread(()->{  //Вход без нажатия на логотип
            try {
                Thread.sleep(1000);
                if(!logoTapped) {
                    startRetrofit();
                    ThisApplication.loadSettings();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

    }


    private void startRetrofit() {
        //Константа принимает первоначальное значение
        DATA_BASE_URL = "http://" + getProp("IP") + ":" + getProp("PORT") + "/";
        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(DATA_BASE_URL);
            Log.d(TAG, "startRetrofit: " + String.format("base url = %s", DATA_BASE_URL));
            //Проверка соединения по доступности пользователя с id = 1
            UserApiInterface api = UserService.getInstance().getApi();
            api.getAll().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        //Если соединение с сервером удалось, то IP сохраняется в файл свойств
                        String userNameInProps = getProp("USER_NAME");
                        if (!userNameInProps.equals("")) {
                            for (User u : response.body()) {
                                if (u.getName().equals(userNameInProps)) {
                                    CURRENT_USER = u;
                                    createLog(true, "Подключился к серверу");
                                }
                            }
                        }
                        runOnUiThread(() -> {
                            Intent searchIntent = new Intent(StartActivity.this, DataLoadingActivity.class);
                            startActivity(searchIntent);
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    Intent intent = new Intent(StartActivity.this, ConnectionToServer.class);
                    startActivity(intent);

                }
            });

        });
        t.start();

    }



}

