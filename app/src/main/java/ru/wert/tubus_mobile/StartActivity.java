package ru.wert.tubus_mobile;

import static ru.wert.tubus_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.tubus_mobile.ThisApplication.getProp;
import static ru.wert.tubus_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.tubus_mobile.constants.Consts.SEND_ERROR_REPORTS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import org.acra.ACRA;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.tubus_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.dataPreloading.CacheManager;
import ru.wert.tubus_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.tubus_mobile.main.BaseActivity;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.servicesREST.UserService;
import ru.wert.tubus_mobile.heartbeat.ConnectionManager;
import ru.wert.tubus_mobile.organizer.OrganizerActivity;

/**
 * Отправная точка работы приложения
 * 1) Запускается Ретрофит
 * 2) Осуществляется проверка доступности базы данных через загрузку пользователя с id = 1
 * 3) Если база доступна, то в таблице логов производится запись о поключении к серверу пользователя,
 *    указанного в файле Properties приложения и открывается Окно загрузки данных,
 *    иначе - открывается Окно подключения к серверу
 * 4) Загружаются настройки приложения
 * 5) Запускается heartbeat для поддержания соединения
 */
public class StartActivity extends BaseActivity implements ConnectionManager.ConnectionStatusListener {

    private static final String TAG = "StartActivity";
    private boolean logoTapped;
    private ConnectionManager connectionManager;
    private CacheManager cacheManager;

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

        // Инициализируем менеджер кэша
        cacheManager = new CacheManager(this);

        // Инициализируем менеджер соединения
        connectionManager = ConnectionManager.getInstance();
        connectionManager.setConnectionStatusListener(this);

        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.setOnClickListener(v -> {
            logoTapped = true;
            startRetrofit();
            ThisApplication.loadSettings();
        });

        new Thread(() -> {  //Вход без нажатия на логотип
            try {
                Thread.sleep(5000);
                if (!logoTapped) {
                    startRetrofit();
                    ThisApplication.loadSettings();

                    ACRA.getErrorReporter().setEnabled(SEND_ERROR_REPORTS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Запускаем heartbeat при возобновлении активности
        if (connectionManager != null) {
            connectionManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Останавливаем heartbeat при паузе активности
        if (connectionManager != null) {
            connectionManager.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Останавливаем heartbeat при уничтожении активности
        if (connectionManager != null) {
            connectionManager.stop();
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                Log.i(TAG, "Соединение с сервером установлено");
                // Можно добавить визуальную индикацию успешного соединения
            } else {
                Log.w(TAG, "Соединение с сервером потеряно");
                // Можно добавить визуальную индикацию потери соединения
            }
        });
    }

    private void startRetrofit() {
        DATA_BASE_URL = "http://" + getProp("IP") + ":" + getProp("PORT") + "/";

        // Проверяем наличие валидного кэша перед установкой соединения
        if (cacheManager.isCacheValid()) {
            Log.i(TAG, "Обнаружен валидный кэш, запускаем загрузку данных");
            runOnUiThread(() -> {
                Intent dataLoading = new Intent(StartActivity.this, DataLoadingActivity.class);
                startActivity(dataLoading);
            });

            // Запускаем фоновую проверку соединения для обновления данных
            startBackgroundConnectionCheck();
            return;
        }

        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(DATA_BASE_URL);
            Log.d(TAG, "startRetrofit: " + String.format("base url = %s", DATA_BASE_URL));

            // Запускаем heartbeat менеджер
            connectionManager.start();

            // Проверка соединения по доступности пользователя с id = 1
            UserApiInterface api = UserService.getInstance().getApi();
            api.getAll().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        handleSuccessfulConnection(response.body());
                    } else {
                        handleConnectionFailure();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    handleConnectionFailure();
                }
            });
        });
        t.start();
    }

    private void handleSuccessfulConnection(List<User> users) {
        String userNameInProps = getProp("USER_NAME");
        if (!userNameInProps.equals("")) {
            for (User u : users) {
                if (u.getName().equals(userNameInProps)) {
                    CURRENT_USER = u;
                    createLog(true, "Подключился к серверу");
                    break;
                }
            }

            runOnUiThread(() -> {
                Intent dataLoading = new Intent(StartActivity.this, DataLoadingActivity.class);
                startActivity(dataLoading);
            });
        } else {
            runOnUiThread(() -> {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            });
        }
    }

    private void handleConnectionFailure() {
        // Проверяем наличие любых кэшированных данных
        if (cacheManager.hasAnyCachedData()) {
            Log.w(TAG, "Соединение с сервером недоступно, но есть кэшированные данные");
            runOnUiThread(() -> {
                new AlertDialog.Builder(StartActivity.this)
                        .setTitle("Оффлайн режим")
                        .setMessage("Сервер недоступен. Используем данные из кэша.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent dataLoading = new Intent(StartActivity.this, DataLoadingActivity.class);
                            startActivity(dataLoading);
                        })
                        .setCancelable(false)
                        .show();
            });
        } else {
            // Нет кэша и нет соединения
            runOnUiThread(() -> {
                if (!ifConnectedToWifi(false)) {
                    new AlertDialog.Builder(StartActivity.this)
                            .setTitle("Внимание!")
                            .setMessage("WiFi не включен")
                            .setPositiveButton("Сейчас включу!", (dialog, which) -> {
                                Intent intent = new Intent(StartActivity.this, ConnectionToServerActivity.class);
                                startActivity(intent);
                            })
                            .show();
                } else {
                    Intent intent = new Intent(StartActivity.this, ConnectionToServerActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void startBackgroundConnectionCheck() {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Даем время основной активности запуститься

                RetrofitClient.setBASE_URL(DATA_BASE_URL);
                UserApiInterface api = UserService.getInstance().getApi();

                // Фоновая проверка соединения для обновления данных
                api.getAll().enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Фоновая проверка соединения: успешно");
                            // Соединение есть, данные будут обновлены в DataLoadingActivity
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        Log.w(TAG, "Фоновая проверка соединения: недоступно");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
