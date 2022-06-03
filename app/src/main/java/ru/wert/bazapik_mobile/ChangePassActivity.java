package ru.wert.bazapik_mobile;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.search.SearchActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChangePassActivity extends AppCompatActivity {

    private static final String TAG = "ChangePassActivity";

    private EditText etOldPass;
    private EditText etNewPass;
    private EditText etRepeatNewPass;
    private Button btnSavePass, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etRepeatNewPass = findViewById(R.id.etRepeatNewPass);

        btnSavePass = findViewById(R.id.btnSaveNewPass);
        btnSavePass.setOnClickListener(e->saveNewPass());

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(e-> {
            Intent intent = new Intent(ChangePassActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void saveNewPass(){
        String oldPass = etOldPass.getText().toString();
        String newPass = etNewPass.getText().toString();
        String repeatNewPass = etRepeatNewPass.getText().toString();

        if(oldPass.isEmpty() || newPass.isEmpty() || repeatNewPass.isEmpty()){
            new WarningDialog1().show(ChangePassActivity.this, "Внимание", "Необходимо заполнить все поля!");
            return;
        }

        int maxLength = 10;
        if(oldPass.length() > maxLength || newPass.length() > maxLength || repeatNewPass.length() > maxLength){
            new WarningDialog1().show(ChangePassActivity.this, "Внимание", "Длина проля превышает допустимые 10 символов!");
            return;
        }

        if(!oldPass.equals(CURRENT_USER.getPassword())){
            new WarningDialog1().show(ChangePassActivity.this, "Внимание", "Старый пароль не подходит!");
            return;
        }
        if(!newPass.equals(repeatNewPass)){
            new WarningDialog1().show(ChangePassActivity.this, "Внимание", "Новые пароли не совпадают!");
            return;
        }

        UserApiInterface api = RetrofitClient.getInstance().getRetrofit().create(UserApiInterface.class);
        CURRENT_USER.setPassword(newPass);
        Call<Void> call = api.update(CURRENT_USER);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new WarningDialog1().show(ChangePassActivity.this, "Внимание!", "Новый пароль успешно сохранен!");
                    AlertDialog alertDialog = new AlertDialog.Builder(ChangePassActivity.this).create();
                    alertDialog.setTitle("Внимание");
                    alertDialog.setMessage("Новый пароль успешно сохранен!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                dialog.dismiss();
                                Intent intent = new Intent(ChangePassActivity.this, LoginActivity.class);
                                startActivity(intent);

                            });
                    alertDialog.show();
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d(TAG, String.format("Ошибка сохранения нового пароля для пользователя %s", CURRENT_USER.getName()));
                new WarningDialog1().show(ChangePassActivity.this, "Внимание!", "Не удалось сохранить новый пароль.");
            }
        });

    }
}