package ru.wert.bazapik_mobile;

import static android.Manifest.permission.INTERNET;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;

public class StartActivity extends BaseActivity {

    private static final String TAG = "StartActivity";
    EditText editTextIp;
    private String ip;
    private String baseUrl;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);

        editTextIp = findViewById(R.id.editTextIp);
        editTextIp.setText(getProp("BASE_IP"));
        Log.i(TAG, "onCreate: BASE_IP = " + getProp("BASE_IP"));

        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.setOnClickListener(v -> startRetrofit());

        editTextIp.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRetrofit();
                return true;
            }
            return false;
        });


    }


    private void startRetrofit() {
        ip = String.valueOf(editTextIp.getText());
        Consts.DATA_BASE_URL = "http://" + ip + ":8080/";

        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(Consts.DATA_BASE_URL);
            Log.d(TAG, "startRetrofit: " + String.format("base url = %s", baseUrl));
            UserService.getInstance().getApi().getById(1L).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        setProp("BASE_IP", ip);
                        checkForPermissions();
                        runOnUiThread(() -> {
//                            Intent loginIntent = new Intent(StartActivity.this, Login.class);
//                            startActivity(loginIntent);
                            Intent searchIntent = new Intent(StartActivity.this, DataLoadingActivity.class);
                            startActivity(searchIntent);
                        });
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    editTextIp.post(() -> editTextIp.setText(getProp("BASE_IP")));
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

