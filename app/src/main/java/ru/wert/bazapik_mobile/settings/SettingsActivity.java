package ru.wert.bazapik_mobile.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.StartActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private CheckBox cbShowFolders;
    private CheckBox cbHidePrefixes;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbShowFolders = findViewById(R.id.cbShowFolders);
        cbHidePrefixes = findViewById(R.id.cbHidePrefixes);
        tvVersion = findViewById(R.id.tvVersion);

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