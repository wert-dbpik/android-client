package ru.wert.bazapik_mobile;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class ConnectionToServer extends BaseActivity {

    private static String TAG = "ConnectionToServer";
    private TextView mIpAddress;
    private TextView mPort;
    private Button mBtnConnect;
    private String ip;
    private String port;
    private String baseUrl;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_to_server);

        mIpAddress = findViewById(R.id.tvIpaddress);
        mIpAddress.setText(getProp("IP"));

        mPort = findViewById(R.id.tvPort);
        mPort.setText(getProp("PORT"));

        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener((event)->{
            startRetrofit();
        });


    }

    private void startRetrofit() {
        ip = String.valueOf(mIpAddress.getText());
        port = String.valueOf(mPort.getText());
        //Меняем константу на новое значение
        DATA_BASE_URL = "http://" + ip + ":" + port + "/";

        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(DATA_BASE_URL);
            Log.i(TAG, "startRetrofit: " + String.format("base url = %s", baseUrl));
            //Проверка соединения по доступности пользователя с id = 1
            UserService.getInstance().getApi().getById(1L).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        //Если соединение с сервером удалось, то IP и PORT сохраняются в файл свойств
                        setProp("IP", ip);
                        setProp("PORT", port);
                        //Интерфейс на поиска пользователей
                        UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
                        String userName = getProp("USER_NAME");
                        if(!userName.equals("")) {
                            Log.d(TAG, "Текущее имя пользователя = " + userName);
                            Call<User> call1 = api.getByName(userName);
                            call1.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, "Пользователь найден: " + userName + "Он становится CURRENT_USER");
                                        CURRENT_USER = user;
                                        Log.d(TAG, "Переходим к загрузке данных в DataLoadingActivity");
                                        goDataLoadActivity();
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    if(t.getMessage().contains("Failed to connect")) {
                                        Log.d(TAG, "Проблемы с доступом к серверу: " + t.getMessage());
                                        new WarningDialog1().show(ConnectionToServer.this, "Внимание", "Сервер не доступен, поробуйте позднее");
                                    } else {
                                        Log.d(TAG, "Пользователь " + userName + " в базе не найден, переходим в LoginActivity");
                                        goLoginActivity();
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Текущее имя пользователя не определено, переходим в LoginActivity");
                            goLoginActivity();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    new WarningDialog1().show(ConnectionToServer.this, "Внимание! ",
                            "Не удалось подключиться к серверу по указанному IP адресу\n" +
                                    "Укажите верный адрес или попробуйте позже. " +
                                    "Возможно, сервер сейчас не доступен.");
                    mIpAddress.post(() -> mIpAddress.setText(getProp("IP")));
                }
            });

        });
        t.start();

    }

    private void goDataLoadActivity() {
        Intent dataLoadIntent = new Intent(ConnectionToServer.this, DataLoadingActivity.class);
        startActivity(dataLoadIntent);
    }

    private void goLoginActivity() {
        Intent loginIntent = new Intent(ConnectionToServer.this, LoginActivity.class);
        startActivity(loginIntent);
    }


}