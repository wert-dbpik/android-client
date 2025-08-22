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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.tubus_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.data.servicesREST.UserService;
import ru.wert.tubus_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.tubus_mobile.main.BaseActivity;
import ru.wert.tubus_mobile.warnings.WarningDialog1;

public class ConnectionToServerActivity extends BaseActivity {

    private static String TAG = "ConnectionToServer";
    private TextView mIpAddress;
    private TextView mPort;
    private Button mBtnConnect;
    private String ip;
    private String port;
    private boolean isReconnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_to_server);

        // Проверяем флаг переподключения
        isReconnect = getIntent().getBooleanExtra("RECONNECT", false);
        Log.d(TAG, "Режим переподключения: " + isReconnect);

        mIpAddress = findViewById(R.id.tvIpaddress);
        mIpAddress.setText(getProp("IP"));

        mPort = findViewById(R.id.tvPort);
        mPort.setText(getProp("PORT"));

        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener((event) -> {
            startRetrofit();
        });
    }

    private void startRetrofit() {
        ip = String.valueOf(mIpAddress.getText());
        port = String.valueOf(mPort.getText());

        // Меняем константу на новое значение
        DATA_BASE_URL = "http://" + ip + ":" + port + "/";

        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(DATA_BASE_URL);
            Log.i(TAG, "startRetrofit: base url = " + DATA_BASE_URL);

            // Проверка соединения по доступности пользователя с id = 1
            UserService.getInstance().getApi().getById(1L).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        // Если соединение с сервером удалось, сохраняем IP и PORT
                        setProp("IP", ip);
                        setProp("PORT", port);

                        if (isReconnect) {
                            // При переподключении сразу переходим к загрузке данных
                            Log.d(TAG, "Переподключение успешно, переходим к DataLoadingActivity");
                            goDataLoadActivity();
                        } else {
                            // При первом подключении проверяем пользователя
                            checkUserAndProceed();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    handleConnectionFailure(t);
                }
            });
        });
        t.start();
    }

    private void checkUserAndProceed() {
        UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
        String userName = getProp("USER_NAME");

        if (!userName.equals("")) {
            Log.d(TAG, "Текущее имя пользователя = " + userName);

            Call<User> call = api.getByName(userName);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        Log.d(TAG, "Пользователь найден: " + userName + ". Он становится CURRENT_USER");
                        CURRENT_USER = user;
                        Log.d(TAG, "Переходим к загрузке данных в DataLoadingActivity");
                        goDataLoadActivity();
                    } else {
                        Log.d(TAG, "Пользователь " + userName + " не найден или ошибка ответа");
                        goLoginActivity();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    handleUserCheckFailure(t);
                }
            });
        } else {
            Log.d(TAG, "Текущее имя пользователя не определено, переходим в LoginActivity");
            goLoginActivity();
        }
    }

    private void handleConnectionFailure(Throwable t) {
        Log.e(TAG, "Ошибка подключения к серверу: " + t.getMessage());

        runOnUiThread(() -> {
            if (t.getMessage() != null && t.getMessage().contains("Failed to connect")) {
                new WarningDialog1().show(ConnectionToServerActivity.this, "Внимание!",
                        "Не удалось подключиться к серверу\n" +
                                "Укажите верный IP адрес или попробуйте позже. " +
                                "Возможно, сервер сейчас не доступен.");
            } else {
                new WarningDialog1().show(ConnectionToServerActivity.this, "Ошибка",
                        "Произошла ошибка при подключении: " + t.getMessage());
            }

            mIpAddress.setText(getProp("IP"));
            mPort.setText(getProp("PORT"));
        });
    }

    private void handleUserCheckFailure(Throwable t) {
        Log.e(TAG, "Ошибка при проверке пользователя: " + t.getMessage());

        runOnUiThread(() -> {
            if (t.getMessage() != null && t.getMessage().contains("Failed to connect")) {
                new WarningDialog1().show(ConnectionToServerActivity.this, "Внимание",
                        "Сервер не доступен, попробуйте позднее");
            } else {
                Log.d(TAG, "Пользователь не найден, переходим в LoginActivity");
                goLoginActivity();
            }
        });
    }

    private void goDataLoadActivity() {
        Intent dataLoadIntent = new Intent(ConnectionToServerActivity.this, DataLoadingActivity.class);
        startActivity(dataLoadIntent);
        finish(); // Закрываем текущую активити
    }

    private void goLoginActivity() {
        Intent loginIntent = new Intent(ConnectionToServerActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish(); // Закрываем текущую активити
    }
}