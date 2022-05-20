package ru.wert.bazapik_mobile;

import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;

/**
 * В классе открывается окно Login, после ввода данных происходит сверка
 * Сначала проверяется наличие в базе пользователя с введенным именем, если пользователь
 * с таким именем существует, то проверяется введенный пароль, иначе пользователю сообщается, что
 * пользователя с таким именем не найдено. Если не верен пароль, то выводится соответтвующее
 * оповещение.
 * При успешном входе на сервер, пользователю открывается окно загрузки данных
 * Класс вызывается также при смене пользователя через меню
 */
public class LoginActivity extends BaseActivity {

    private static String TAG = "LoginActivity";
    private TextView mPincode;
    private AutoCompleteTextView mUserName;
    private Button mEnter;
    private static int countOfNumbers;
    private List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserName = findViewById(R.id.autoCompleteUserNameTextView);
        mUserName.setText(getProp("USER_NAME"));
        mUserName.setThreshold(1);//Начинаем подсказывать с 3 символа
        fillWithUsers(mUserName);

        mPincode = findViewById(R.id.tvPincode);
        mEnter = findViewById(R.id.btnEnter);

        mEnter.setOnClickListener((event)->{

            UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
            Log.d(TAG, "Ищем пользователя по введенному паролю");
            Call<User> call = api.getByName(String.valueOf(mUserName.getText()));
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful()){
                        User currentUser = response.body();

                        if(currentUser.getPassword().equals(mPincode.getText().toString())) {
                            Log.d(TAG, String.format("Пользователь '%s' становится CURRENT_USER", currentUser.getName()));
                            CURRENT_USER = currentUser;
                            createLog(true, "Подключился к серверу");
                            setProp("USER_NAME", currentUser.getName());
                            Log.d(TAG, "Переходим в DataLoadingActivity");
                            Intent searchIntent = new Intent(LoginActivity.this, DataLoadingActivity.class);
                            startActivity(searchIntent);
                        } else {
                            Log.d(TAG, "Пароль не подходит");
                            new Warning1().show(LoginActivity.this, "Внимание!", "Пароль не подходит.");
                        }
                    } else {
                        Log.d(TAG, String.format("Пользователя с именем '%s' не найдено", mUserName.getText()));
                        new Warning1().show(LoginActivity.this, "Внимание!", "Пользователь не найден.");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.d(TAG, String.format("Пользователя с именем '%s' не найдено", mUserName.getText()));
                    new Warning1().show(LoginActivity.this, "Внимание!", "Пользователь не найден.");
                }
            });
        });

    }

    private void fillWithUsers(AutoCompleteTextView view) {
        UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
        Call<List<User>> call = api.getAll();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    List<User> allUsers = response.body();

                    names = new AbstractList<String>() {
                        @Override
                        public int size() {
                            return allUsers.size();
                        }

                        @Override
                        public String get(int location) {
                            return allUsers.get(location).getName();
                        }
                    };
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names);
                    view.setAdapter(adapter);

                } else {
                    handleBadRequest(response, TAG);
                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                handleOnFailure(t, TAG);
            }
        });

    }
}