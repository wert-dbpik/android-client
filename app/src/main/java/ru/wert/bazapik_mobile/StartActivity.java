package ru.wert.bazapik_mobile;

import static android.Manifest.permission.INTERNET;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;
import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_FOLDERS;
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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;
import ru.wert.bazapik_mobile.warnings.Warning1;

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
        Log.i(TAG, "onCreate: SHOW_FOLDERS = " + getProp("SHOW_FOLDERS"));
        Log.i(TAG, "onCreate: HIDE_PREFIXES = " + getProp("HIDE_PREFIXES"));

        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.setOnClickListener(v -> {
            logoTapped = true;
            startRetrofit();
            ThisApplication.loadSettings();
        });

        new Thread(()->{  //Вход без нажатия на логотип
            try {
                Thread.sleep(2000);
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
        checkForPermissions();
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


    private void checkForPermissions() {

        if (StartActivity.this.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPerms();
        }
        if (StartActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPerms();
        }
        if(StartActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                StartActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            showPPP();

    }

    private void showPPP() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Ахтунг!")
                    .setMessage("Если Вы не подтвердите все разрешения, то\n" +
                            "Вы и ваши предки до 7го колена\n" +
                            "пудут подвергнуты анафеме,\n" +
                            "а Ваша яхта и домик в деревне\n" +
                            "будут изъяты в пользу ООО НТЦ ПИК")
                    .setPositiveButton("Я подумаю", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkForPermissions();
                        }
                    })
                    .setNegativeButton("Я гордый", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            System.exit(0);
                        }
                    })
                    .create().show();
        }
    }

    private void requestPerms(){
        String[] perm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(StartActivity.this,perm,123);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Храни Господь!}", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Гори в аду!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

