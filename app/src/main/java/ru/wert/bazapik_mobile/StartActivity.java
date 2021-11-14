package ru.wert.bazapik_mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

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
            if(actionId == EditorInfo.IME_ACTION_DONE){
                startRetrofit();
                return true;
            }
            return false;
        });


    }


    private void startRetrofit(){
        ip = String.valueOf(editTextIp.getText());
        Consts.DATA_BASE_URL = "http://" + ip + ":8080/";

        Thread t = new Thread(() -> {
            RetrofitClient.setBASE_URL(Consts.DATA_BASE_URL);
            Log.d(TAG, "startRetrofit: " + String.format("base url = %s", baseUrl) );
            UserService.getInstance().getApi().getById(1L).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful()) {
                        setProp("BASE_IP", ip);
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
                    editTextIp.post(()->editTextIp.setText(getProp("BASE_IP")));
                }
            });

        });
        t.start();

    }

}









/*

    private void startRetrofit(){
        ip = String.valueOf(editTextIp.getText());
        baseUrl = "http://" + ip + ":8080/";

        Thread t = new Thread(() -> {

            try {
                Retrofit retrofit = RetrofitClient.getInstance(baseUrl).getRetrofit();
                User user = UserService.getInstance().getById(1L);
                if(user != null)
                    runOnUiThread(() -> {
                        Intent loginIntent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(loginIntent);
                    });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Intent startIntent = new Intent(StartActivity.this, StartActivity.class);
                    startActivity(startIntent);
                });
            }
        });
        t.start();

    }*/
