package ru.wert.bazapik_mobile.settings;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.loadSettings;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;
import static ru.wert.bazapik_mobile.constants.Consts.SEND_ERROR_REPORTS;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.widget.CheckBox;
import android.widget.TextView;

import org.acra.ACRA;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.util.DownloadFileTask;
import ru.wert.bazapik_mobile.main.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private CheckBox cbShowSolidFiles;
    private CheckBox cbHidePrefixes;
    private CheckBox cbSendErrorReports;
    private CheckBox cbUseAppKeyboard;
    private TextView tvVersion;
    private TextView tvVersionAvailable, tvLoadEDrawings;
    private AsyncTask<String, String, Boolean> downloadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbShowSolidFiles = findViewById(R.id.cbShowSolidFiles);
        cbHidePrefixes = findViewById(R.id.cbHidePrefixes);
        cbSendErrorReports = findViewById(R.id.cbSendErrorMails);
        cbUseAppKeyboard = findViewById(R.id.cbUseAppKeyboard);
        tvVersion = findViewById(R.id.tvVersion);
        tvVersionAvailable = findViewById(R.id.tvVersionAvailable);
        tvLoadEDrawings = findViewById(R.id.tvLoadEDrawings);

        tvLoadEDrawings.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        tvLoadEDrawings.setOnClickListener(e->{
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("ВНИМАНИЕ!")
                    .setMessage( "Файл 'eDrawings.apk' будет сохранен в папку Загрузки")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                        String fileName = "eDrawings.apk";
                        File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                        downloadTask = new DownloadFileTask(
                                this,
                                "apk",
                                destinationFolder.toString());
                        downloadTask.execute(fileName);

                    }).create().show();

        });

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
                                "TubusMobile-" + APPLICATION_VERSION_AVAILABLE))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                            String fileName = "TubusMobile-" + APPLICATION_VERSION_AVAILABLE + ".apk";
                            File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                            downloadTask = new DownloadFileTask(
                                            this,
                                            "apk",
                                            destinationFolder.toString());
                            downloadTask.execute(fileName);

                        }).create().show();

            });
        }else
            tvVersionAvailable.setText("Это beta версия");

        //Кликабельная ссылка
        TextView tvVideo = findViewById(R.id.tvVideo);
        tvVideo.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void onResume() {
        super.onResume();
        cbShowSolidFiles.setChecked(Boolean.parseBoolean(getProp("SHOW_SOLID_FILES")));
        cbHidePrefixes.setChecked(Boolean.parseBoolean(getProp("HIDE_PREFIXES")));
        cbSendErrorReports.setChecked(Boolean.parseBoolean(getProp("SEND_ERROR_REPORTS")));
        cbUseAppKeyboard.setChecked(Boolean.parseBoolean(getProp("USE_APP_KEYBOARD")));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setProp("SHOW_SOLID_FILES", String.valueOf(cbShowSolidFiles.isChecked()));
        setProp("HIDE_PREFIXES", String.valueOf(cbHidePrefixes.isChecked()));
        setProp("SEND_ERROR_REPORTS", String.valueOf(cbSendErrorReports.isChecked()));
        setProp("USE_APP_KEYBOARD", String.valueOf(cbUseAppKeyboard.isChecked()));

        loadSettings();

        ACRA.getErrorReporter().setEnabled(SEND_ERROR_REPORTS);

    }
}