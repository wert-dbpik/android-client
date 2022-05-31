package ru.wert.bazapik_mobile.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.StartActivity;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.main.BaseActivity;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.getProp;
import static ru.wert.bazapik_mobile.ThisApplication.loadSettings;
import static ru.wert.bazapik_mobile.ThisApplication.setProp;

public class SettingsActivity extends BaseActivity {

    private CheckBox cbShowFolders;
    private CheckBox cbHidePrefixes;
    private TextView tvVersion;
    private TextView tvVersionAvalable;

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
        }else
            tvVersionAvalable.setText("Это beta версия");
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