package ru.wert.bazapik_mobile.menus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.search.SearchActivity;

public class FirstMenuActivity extends BaseActivity {

    private Button mBtnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_menu);

        //Получаем временную папку
        Consts.TEMP_DIR = FirstMenuActivity.this.getCacheDir(); // context being the Activity pointer

        mBtnSearch = findViewById(R.id.btnSearch);
        mBtnSearch.setOnClickListener((e)->{
            Intent intent = new Intent(FirstMenuActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }
}