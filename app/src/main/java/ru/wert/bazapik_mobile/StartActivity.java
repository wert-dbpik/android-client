package ru.wert.bazapik_mobile;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.getAppContext;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.constants.Consts.SEND_ERROR_REPORTS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import org.acra.ACRA;
import org.acra.util.ToastSender;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;

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
        Log.i(TAG, "onCreate: SEND_ERROR_REPORTS = " + getProp("SEND_ERROR_REPORTS"));
        Log.i(TAG, "onCreate: USE_APP_KEYBOARD = " + getProp("USE_APP_KEYBOARD"));

        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.setOnClickListener(v -> {
            logoTapped = true;
            startRetrofit();
            ThisApplication.loadSettings();
        });

        new Thread(()->{  //Вход без нажатия на логотип
            try {
                Thread.sleep(5000);
                if(!logoTapped) {
                    startRetrofit();
                    ThisApplication.loadSettings();

                    ACRA.getErrorReporter().setEnabled(SEND_ERROR_REPORTS);

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
                            runOnUiThread(() -> {
                                Intent dataLoading = new Intent(StartActivity.this, DataLoadingActivity.class);
                                startActivity(dataLoading);
                            });
                        }else {
                            runOnUiThread(() -> {
                                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    if (!ifConnectedToWifi(false))
                        new AlertDialog.Builder(StartActivity.this)
                                .setTitle("Внимание!")
                                .setMessage("Wifi не включен")
                                .setPositiveButton("Сейчас включу!", (dialog, which) -> {
                                    Intent intent = new Intent(StartActivity.this, ConnectionToServerActivity.class);
                                    startActivity(intent);
                                })
                                .show();
                    else {
                        Intent intent = new Intent(StartActivity.this, ConnectionToServerActivity.class);
                        startActivity(intent);
                    }
                }
            });

        });
        t.start();

    }



}

