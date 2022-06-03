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

import androidx.appcompat.app.AlertDialog;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.warnings.Warning2;
import ru.wert.bazapik_mobile.warnings.WarningDialog2;

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
            tvVersionAvailable.setText(String.format("Загрузить новую версию %s", APPLICATION_VERSION_AVAILABLE));
            tvVersionAvailable.setTextColor(Color.YELLOW);

            tvVersionAvailable.startAnimation(ThisApplication.createBlinkAnimation());

            tvVersionAvailable.setOnClickListener(e->{
                tvVersionAvailable.clearAnimation();

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("ВНИМАНИЕ!")
                        .setMessage( String.format("Файл с новой версией приложения %s будет сохранен в папку Загрузки",
                                "BazaPIK-" + APPLICATION_VERSION_AVAILABLE))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                            String fileName = "BazaPIK-" + APPLICATION_VERSION_AVAILABLE + ".apk";
                            File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            FILE_SERVICE.download("apk", fileName, destinationFolder.toString(), SettingsActivity.this);
                        }).create().show();

            });
        }else
            tvVersionAvailable.setText("Это beta версия");

        //Кликабельная ссылка
        TextView textView = findViewById(R.id.tvVideo);
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