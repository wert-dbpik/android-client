package ru.wert.bazapik_mobile.settings;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.FILE_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.loadSettings;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.warnings.Warning2;

public class SettingsActivity extends BaseActivity {

    private CheckBox cbShowFolders;
    private CheckBox cbHidePrefixes;
    private TextView tvVersion;
    private TextView tvVersionAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbShowFolders = findViewById(R.id.cbShowFolders);
        cbHidePrefixes = findViewById(R.id.cbHidePrefixes);
        tvVersion = findViewById(R.id.tvVersion);
        tvVersionAvailable = findViewById(R.id.tvVersionAvalable);

        tvVersion.setText(ThisApplication.APPLICATION_VERSION);

        if (APPLICATION_VERSION_AVAILABLE.compareTo(APPLICATION_VERSION) == 0)
            tvVersionAvailable.setText("Это последняя версия");
        else if (APPLICATION_VERSION_AVAILABLE.compareTo(APPLICATION_VERSION) > 0) {
            tvVersionAvailable.setText(String.format("Доступна новая версия: %s", APPLICATION_VERSION_AVAILABLE));
            tvVersionAvailable.setTextColor(Color.YELLOW);

            tvVersionAvailable.startAnimation(ThisApplication.createBlinkAnimation());

            tvVersionAvailable.setOnClickListener(e->{
                tvVersionAvailable.clearAnimation();
                Warning2 warningDialog = new Warning2(SettingsActivity.this,
                        "ВНИМАНИЕ!",
                        String.format("Файл с новой версией приложения %s будет сохранен в папку Загрузки",
                                "BazaPIK-" + APPLICATION_VERSION_AVAILABLE));

                warningDialog.setOnDismissListener(dialogInterface -> {
                    String fileName = "BazaPIK-" + APPLICATION_VERSION_AVAILABLE + ".apk";
                    File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    Log.i("DownloadingFile", "destinationFolder = " + destinationFolder);
                    FILE_SERVICE.download("apk", fileName, destinationFolder.toString(), SettingsActivity.this);

                });

                warningDialog.show();

            });
        }else
            tvVersionAvailable.setText("Это beta версия");

        //Кликабельная ссылка
        TextView textView = (TextView)findViewById(R.id.tvVideo);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

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