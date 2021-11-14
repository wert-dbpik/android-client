package ru.wert.bazapik_mobile.info;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.search.ItemRecViewAdapter;
import ru.wert.bazapik_mobile.search.SearchActivity;
import ru.wert.bazapik_mobile.viewer.PdfViewerActivity;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class PassportInfoActivity extends BaseActivity  implements PassportRecViewAdapter.PassportClickListener{

    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private PassportRecViewAdapter mAdapter;
    private Long passId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport_info);
        //Получаем Id пасспорта
        passId = Long.parseLong(getIntent().getStringExtra("PASSPORT_ID"));


        tvDecNumber = findViewById(R.id.tvDecNumber);//Децимальный номер пасспорта

        tvName = findViewById(R.id.tvName); //Наименование пасспорта

        tvDrafts = findViewById(R.id.tvDrafts);//Строка Доступные чертежи

        rvDrafts = findViewById(R.id.rvDrafts); //RecycleView


        new Thread(()->{
            Passport passport = PassportService.getInstance().findById(passId);
            String decNum = passport.getPrefix() == null ?
                    passport.getNumber() :
                    passport.getPrefix().getName() + "." + passport.getNumber();

            runOnUiThread(()->{
                tvDecNumber.setText(decNum);
                tvName.setText(passport.getName());
                tvDrafts.setText("Доступные чертежи");

            });
        }).start();
        createRecycleViewOfFoundItems();


    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        rvDrafts.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            List<Draft> drafts = DraftService.getInstance().findByPassportId(passId);
            runOnUiThread(() -> {
                mAdapter = new PassportRecViewAdapter(this, drafts);
                mAdapter.setClickListener(this);
                rvDrafts.setAdapter(mAdapter);
            });
        }).start();

        //Для красоты используем разделитель между элементами списка
        rvDrafts.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(PassportInfoActivity.this, PdfViewerActivity.class);
        intent.putExtra("DRAFT_ID", String.valueOf(mAdapter.getItem(position).getId()));
        startActivity(intent);
    }


}