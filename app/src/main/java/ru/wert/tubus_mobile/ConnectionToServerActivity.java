package ru.wert.tubus_mobile;

import static ru.wert.tubus_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.tubus_mobile.ThisApplication.getProp;
import static ru.wert.tubus_mobile.ThisApplication.setProp;
import static ru.wert.tubus_mobile.constants.Consts.CURRENT_USER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.tubus_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.servicesREST.UserService;
import ru.wert.tubus_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.tubus_mobile.main.BaseActivity;
import ru.wert.tubus_mobile.socketwork.SocketService;
import ru.wert.tubus_mobile.warnings.WarningDialog1;

/**
 * Активность для подключения к серверу и настройки параметров соединения.
 * Обеспечивает проверку доступности сервера, сохранение настроек и управление socket-соединением.
 */
public class ConnectionToServerActivity extends BaseActivity {

    private static final String TAG = "ConnectionToServer";

    private TextView mIpAddress;
    private TextView mPort;
    private Button mBtnConnect;
    private String ip;
    private String port;
    private boolean isReconnect = false;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_to_server);

        // Проверяем флаг переподключения из интента
        isReconnect = getIntent().getBooleanExtra("RECONNECT", false);
        Log.d(TAG, "Режим работы: " + (isReconnect ? "ПЕРЕПОДКЛЮЧЕНИЕ" : "ПЕРВОЕ ПОДКЛЮЧЕНИЕ"));

        initViews();
        setupButtonListeners();
        loadCurrentSettings();
    }

    /**
     * Инициализация view-компонентов
     */
    private void initViews() {
        mIpAddress = findViewById(R.id.tvIpaddress);
        mPort = findViewById(R.id.tvPort);
        mBtnConnect = findViewById(R.id.btnConnect);
    }

    /**
     * Загрузка текущих настроек из SharedPreferences
     */
    private void loadCurrentSettings() {
        mIpAddress.setText(getProp("IP"));
        mPort.setText(getProp("PORT"));
    }

    /**
     * Настройка обработчиков кнопок
     */
    private void setupButtonListeners() {
        mBtnConnect.setOnClickListener((event) -> {
            startRetrofit();
        });
    }

    /**
     * Запуск процесса проверки подключения к серверу через Retrofit
     * с последующей настройкой SocketService
     */
    private void startRetrofit() {
        ip = String.valueOf(mIpAddress.getText()).trim();
        port = String.valueOf(mPort.getText()).trim();

        // Валидация введенных данных
        if (!validateInput(ip, port)) {
            return;
        }

        // Формирование базового URL
        DATA_BASE_URL = "http://" + ip + ":" + port + "/";
        Log.i(TAG, "Формирование базового URL: " + DATA_BASE_URL);

        showProgressDialog("Проверка подключения к серверу...");

        // Запуск в отдельном потоке для избежания блокировки UI
        new Thread(this::checkServerConnection).start();
    }

    /**
     * Проверка валидности введенных данных
     * @param ip IP-адрес сервера
     * @param port порт сервера
     * @return true если данные валидны
     */
    private boolean validateInput(String ip, String port) {
        if (ip.isEmpty()) {
            showWarning("Ошибка", "Пожалуйста, укажите IP-адрес сервера");
            return false;
        }

        if (port.isEmpty()) {
            showWarning("Ошибка", "Пожалуйста, укажите порт сервера");
            return false;
        }

        if (!port.matches("\\d+")) {
            showWarning("Ошибка", "Порт должен содержать только цифры");
            return false;
        }

        return true;
    }

    /**
     * Проверка соединения с сервером через REST API
     */
    private void checkServerConnection() {
        RetrofitClient.setBASE_URL(DATA_BASE_URL);

        // Проверка доступности сервера через запрос пользователя с id = 1
        UserService.getInstance().getApi().getById(1L).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                dismissProgressDialog();

                if (response.isSuccessful() && response.body() != null) {
                    handleConnectionSuccess();
                } else {
                    handleConnectionFailure(new Exception("Сервер ответил с ошибкой: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dismissProgressDialog();
                handleConnectionFailure(t);
            }
        });
    }

    /**
     * Обработка успешного подключения к серверу
     */
    private void handleConnectionSuccess() {
        Log.i(TAG, "Подключение к серверу успешно установлено");

        // Сохраняем новые настройки
        setProp("IP", ip);
        setProp("PORT", port);

        // Перезапускаем SocketService с новыми параметрами
        restartSocketService();

        runOnUiThread(() -> {
            if (isReconnect) {
                Log.d(TAG, "Переподключение успешно, переход к загрузке данных");
                goToDataLoadActivity();
            } else {
                checkUserAndProceed();
            }
        });
    }

    /**
     * Перезапуск SocketService с новыми параметрами сервера
     */
    private void restartSocketService() {
        try {
            // Останавливаем текущий сервис
            SocketService.getInstance().stop();
            Thread.sleep(500); // Даем время на корректное завершение

            // Запускаем с новыми настройками
            SocketService.getInstance().start();
            Log.d(TAG, "SocketService успешно перезапущен с новыми параметрами");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Ошибка при перезапуске SocketService: " + e.getMessage());
            showWarning("Ошибка", "Не удалось перезапустить сетевое соединение");
        }
    }

    /**
     * Проверка существования пользователя и переход к следующему экрану
     */
    private void checkUserAndProceed() {
        UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
        String userName = getProp("USER_NAME");

        if (userName != null && !userName.isEmpty()) {
            Log.d(TAG, "Поиск пользователя: " + userName);

            showProgressDialog("Проверка пользователя...");

            Call<User> call = api.getByName(userName);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    dismissProgressDialog();

                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        CURRENT_USER = user;
                        Log.d(TAG, "Пользователь найден: " + userName);
                        goToDataLoadActivity();
                    } else {
                        Log.d(TAG, "Пользователь " + userName + " не найден");
                        goToLoginActivity();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    dismissProgressDialog();
                    handleUserCheckFailure(t);
                }
            });
        } else {
            Log.d(TAG, "Имя пользователя не задано");
            goToLoginActivity();
        }
    }

    /**
     * Обработка ошибки подключения к серверу
     * @param t исключение
     */
    private void handleConnectionFailure(Throwable t) {
        Log.e(TAG, "Ошибка подключения к серверу: " + t.getMessage());

        // Останавливаем SocketService при ошибке
        SocketService.getInstance().stop();

        runOnUiThread(() -> {
            String errorMessage = "Не удалось подключиться к серверу\n";

            if (t.getMessage() != null) {
                if (t.getMessage().contains("Failed to connect")) {
                    errorMessage += "Укажите верный IP адрес или попробуйте позже.\n";
                    errorMessage += "Возможно, сервер сейчас не доступен.";
                } else if (t.getMessage().contains("timeout")) {
                    errorMessage += "Превышено время ожидания ответа от сервера.";
                } else {
                    errorMessage += "Ошибка: " + t.getMessage();
                }
            }

            showWarning("Ошибка подключения", errorMessage);
            loadCurrentSettings(); // Восстанавливаем предыдущие настройки
        });
    }

    /**
     * Обработка ошибки при проверке пользователя
     * @param t исключение
     */
    private void handleUserCheckFailure(Throwable t) {
        Log.e(TAG, "Ошибка при проверке пользователя: " + t.getMessage());

        runOnUiThread(() -> {
            if (t.getMessage() != null && t.getMessage().contains("Failed to connect")) {
                showWarning("Ошибка", "Сервер недоступен, попробуйте позднее");
            } else {
                goToLoginActivity();
            }
        });
    }

    /**
     * Переход к активности загрузки данных
     */
    private void goToDataLoadActivity() {
        Log.d(TAG, "Переход к DataLoadingActivity");

        Intent dataLoadIntent = new Intent(ConnectionToServerActivity.this, DataLoadingActivity.class);
        startActivity(dataLoadIntent);
        finish(); // Закрываем текущую активность
    }

    /**
     * Переход к активности авторизации
     */
    private void goToLoginActivity() {
        Log.d(TAG, "Переход к LoginActivity");

        // Останавливаем SocketService при переходе к логину
        SocketService.getInstance().stop();

        Intent loginIntent = new Intent(ConnectionToServerActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish(); // Закрываем текущую активность
    }

    /**
     * Показать диалог прогресса
     * @param message сообщение для отображения
     */
    private void showProgressDialog(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message);
            builder.setCancelable(false);
            progressDialog = builder.create();
            progressDialog.show();
        });
    }

    /**
     * Скрыть диалог прогресса
     */
    private void dismissProgressDialog() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    /**
     * Показать предупреждающее сообщение
     * @param title заголовок
     * @param message сообщение
     */
    private void showWarning(String title, String message) {
        runOnUiThread(() -> {
            new WarningDialog1().show(ConnectionToServerActivity.this, title, message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Скрываем диалог прогресса при уничтожении активности
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // Останавливаем SocketService если это не переподключение
        if (!isReconnect) {
            SocketService.getInstance().stop();
        }
    }
}