package ru.wert.bazapik_mobile.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ConnectionToServer;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.StartActivity;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.servicesREST.FileService;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.search.FilterDialog;
import ru.wert.bazapik_mobile.search.SearchActivity;
import ru.wert.bazapik_mobile.viewer.DownloadDraftTask;
import ru.wert.bazapik_mobile.viewer.ProgressIndicatorFragment;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;
import ru.wert.bazapik_mobile.warnings.Warning2;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.DRAFT_QUICK_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.FILE_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.loadSettings;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;

public class SettingsActivity extends BaseActivity {

    private CheckBox cbShowFolders;
    private CheckBox cbHidePrefixes;
    private TextView tvVersion;
    private TextView tvVersionAvalable;
    private String remoteFileString, localFileString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbShowFolders = findViewById(R.id.cbShowFolders);
        cbHidePrefixes = findViewById(R.id.cbHidePrefixes);
        tvVersion = findViewById(R.id.tvVersion);
        tvVersionAvalable = findViewById(R.id.tvVersionAvalable);

        tvVersion.setText(ThisApplication.APPLICATION_VERSION);

        if (APPLICATION_VERSION_AVAILABLE.compareTo(APPLICATION_VERSION) == 0)
            tvVersionAvalable.setText("Это последняя версия");
        else if (APPLICATION_VERSION_AVAILABLE.compareTo(APPLICATION_VERSION) > 0) {
            tvVersionAvalable.setText(String.format("Доступна новая версия: %s", APPLICATION_VERSION_AVAILABLE));
            tvVersionAvalable.setTextColor(Color.YELLOW);

            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(400); //You can manage the time of the blink with this parameter
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            tvVersionAvalable.startAnimation(anim);

            tvVersionAvalable.setOnClickListener(e->{
                tvVersionAvalable.clearAnimation();
                Warning2 warningDialog = new Warning2(SettingsActivity.this,
                        "ВНИМАНИЕ!",
                        String.format("Файл с новой версией приложения %s будет сохранен в папку Загрузки", "BazaPIK-" + APPLICATION_VERSION_AVAILABLE));

                warningDialog.setOnDismissListener(dialogInterface -> {
                    showProgressIndicator();
                    //Этот поток позволяет показать ProgressIndicator
                    new Thread(()->{
                        String dbdir = DATA_BASE_URL + "drafts/download/apk/";
                        String fileName = "BazaPIK-" + APPLICATION_VERSION_AVAILABLE;
                        String ext = ".apk";
                        File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        remoteFileString = dbdir + fileName + ext;
                        File localDraftFile = new File(destinationFolder.toString() + "/" + fileName + ext);

                        try {
                            //Запускаем асинхронную задачу по загрузке файла чертежа
                            String res = new DownloadDraftTask().execute(remoteFileString, localFileString).get();
                            if (res.equals("OK")) {
                                Log.d("DownloadingNewVersion", String.format("File '%s' was downloaded with OK message", "BazaPIK-" + APPLICATION_VERSION_AVAILABLE));
                                runOnUiThread(()->{
                                    new Warning1().show(SettingsActivity.this, "Внимание!",
                                            "Файл с новой версией успешно загружен!");
                                });
                            } else {
                                Log.e("DownloadingNewVersion", String.format("remoteFileString = '%s', localFileString = '%s', message from server: %s",
                                        remoteFileString, localFileString, res));
                                runOnUiThread(()->{
                                    new Warning1().show(SettingsActivity.this, "Внимание!",
                                            "Не удалось загрузить файл , возможно, сервер не доступен.");
                                });
                            }

                        } catch (ExecutionException | InterruptedException ex) {
                            Log.e("DownloadingNewVersion", "could not download file from server by error: " + ex.toString());
                            runOnUiThread(()->{
                                new Warning1().show(SettingsActivity.this, "Внимание!",
                                        "Не удалось загрузить файл чертежа, возможно, сервер не доступен.");
                            });
                        }

                    }).start();

//                    new Thread(()->{
//                        String fileName = "BazaPIK-" + APPLICATION_VERSION_AVAILABLE;
//                        String ext = ".apk";
//                        File destinationDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                        FILE_SERVICE.download("apk", fileName, ext, destinationDir.toString());
//                        runOnUiThread(()->{
//                            Intent intent = new Intent(SettingsActivity.this, SearchActivity.class);
//                            startActivity(intent);
//                        });
//                    }).start();

                });

                warningDialog.show();

            });
        }else
            tvVersionAvalable.setText("Это beta версия");

        //Кликабельная ссылка
        TextView textView = (TextView)findViewById(R.id.tvVideo);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void showProgressIndicator() {
        Fragment progressIndicatorFragment = new ProgressIndicatorFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.settings_fragment_container, progressIndicatorFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cbShowFolders.setChecked(Boolean.parseBoolean(getProp("SHOW_FOLDERS")));
        cbHidePrefixes.setChecked(Boolean.parseBoolean(getProp("HIDE_PREFIXES")));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setProp("SHOW_FOLDERS", String.valueOf(cbShowFolders.isChecked()));
        setProp("HIDE_PREFIXES", String.valueOf(cbHidePrefixes.isChecked()));

        loadSettings();

    }
}