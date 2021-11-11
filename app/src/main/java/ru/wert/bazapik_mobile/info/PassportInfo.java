package ru.wert.bazapik_mobile.info;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.search.ItemRecViewAdapter;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class PassportInfo extends BaseActivity  implements PassportRecViewAdapter.PassportClickListener{

    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private PassportRecViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport_info);

        //Децимальный номер пасспорта
        tvDecNumber = findViewById(R.id.tvDecNumber);
        //Наименование пасспорта
        tvName = findViewById(R.id.tvName);
        tvDrafts = findViewById(R.id.tvDrafts);
        //Доступные чертежи
        rvDrafts = findViewById(R.id.rvDrafts);
        String passId = getIntent().getStringExtra("PASSPORT_ID");

        new Thread(()->{
            Passport passport = PassportService.getInstance().findById(Long.parseLong(passId));
            String decNum = passport.getPrefix() == null ?
                    passport.getNumber() :
                    passport.getPrefix().getName() + "." + passport.getNumber();
            List<Draft> drafts = DraftService.getInstance().findByPassportId(Long.parseLong(passId));
            runOnUiThread(()->{
                tvDecNumber.setText(decNum);
                tvName.setText(passport.getName());
                tvDrafts.setText("Доступные чертежи");
                createRecycleViewOfFoundItems(drafts);
            });
        }).start();

    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems(List<Draft> drafts) {

        rvDrafts.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
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

    }
}