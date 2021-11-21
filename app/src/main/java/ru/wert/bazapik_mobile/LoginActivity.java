package ru.wert.bazapik_mobile;

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
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;

public class LoginActivity extends BaseActivity {

    private static String TAG = "LoginActivity";
    private TextView mPincode;
    private Button mEnter;
    private static int countOfNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPincode = findViewById(R.id.tvPincode);
        mEnter = findViewById(R.id.btnEnter);

        mEnter.setOnClickListener((event)->{

            UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
            Log.d(TAG, "Ищем пользователя по введенному паролю");
            Call<User> call = api.getByPassword(String.valueOf(mPincode.getText()));
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful()){
                        User currentUser = response.body();

                        Log.d(TAG, "Пользователь найден: " + currentUser.getName() + "Он становится CURRENT_USER");
                        CURRENT_USER = currentUser;
                        setProp("USER_NAME", currentUser.getName());
                        Log.d(TAG, "Переходим в DataLoadingActivity");
                        Intent searchIntent = new Intent(LoginActivity.this, DataLoadingActivity.class);
                        startActivity(searchIntent);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    if(t.getMessage().contains("Failed to connect")) {
                        Log.d(TAG, "Проблемы с доступом к серверу: " + t.getMessage());
                        Warning1.show(LoginActivity.this, "Внимание", "Сервер не доступен, поробуйте позднее");
                    } else {
                        Log.d(TAG, "Пользователь по введенному паролю не найден, сообщаем о проблеме и повторяем запрос");
                        Warning1.show(LoginActivity.this, "Внимание!", "Пользователь с таким паролем не найден, введите пароль еще раз.");
                    }
                }
            });
        });

    }
}