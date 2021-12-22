package ru.wert.bazapik_mobile.info;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class PassportInfoActivity extends BaseActivity  implements PassportRecViewAdapter.PassportClickListener{
    private static final String TAG = "+++++ PassportInfoActivity +++++" ;
    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private PassportRecViewAdapter mAdapter;
    private Long passId;

    //Меню
    @Getter @Setter private boolean showValid = true;
    @Getter @Setter private boolean showChanged = true;
    @Getter @Setter private boolean showAnnulled = true;

    //Все найденные элементы
    private List<Draft> foundDrafts;


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
            Passport passport = ThisApplication.PASSPORT_SERVICE.findById(passId);
            String decNum;
            if(passport != null) {
                decNum = passport.getPrefix() == null ?
                        passport.getNumber() :
                        passport.getPrefix().getName() + "." + passport.getNumber();

                runOnUiThread(() -> {
                    tvDecNumber.setText(decNum);
                    tvName.setText(passport.getName());
                    tvDrafts.setText("Доступные чертежи");

                });
            } else {
                Log.e(TAG, String.format("An error occur while trying to get Passport of id = %s, PASSPORT_SERVICE = %s"
                        ,passId, ThisApplication.PASSPORT_SERVICE));
                runOnUiThread(()->{
                    new Warning1().show(PassportInfoActivity.this,
                            "Ошибка!", "Что-то пошло не так, вероятно потреяна связь с сервером.");
                });

            }
        }).start();
        createRecycleViewOfFoundItems();


    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        rvDrafts.setLayoutManager(new LinearLayoutManager(this));

//        new Thread(() -> {
//            foundDrafts = DraftService.getInstance().findByPassportId(passId);
//            filterList(foundDrafts);
//            runOnUiThread(() -> {
//                mAdapter = new PassportRecViewAdapter(this, foundDrafts);
//                mAdapter.setClickListener(this);
//                rvDrafts.setAdapter(mAdapter);
//            });
//        }).start();

        DraftApiInterface api = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
        Call<List<Draft>> call =  api.getByPassportId(passId);
        call.enqueue(new Callback<List<Draft>>() {
            @Override
            public void onResponse(Call<List<Draft>> call, Response<List<Draft>> response) {
                if(response.isSuccessful()) {
                    mAdapter = new PassportRecViewAdapter(PassportInfoActivity.this, response.body());
                    mAdapter.setClickListener(PassportInfoActivity.this);
                    rvDrafts.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Draft>> call, Throwable t) {
                new Warning1().show(PassportInfoActivity.this, "Внимание!","Проблемы на линии!");
            }
            
        });

        //Для красоты используем разделитель между элементами списка
        rvDrafts.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    /**
     * Метод фильтрует переданный список чертежей по статусу
     * @param items List<Draft>
     */
    public void filterList(List<Draft> items) {
        if(items.isEmpty()) return;
        Iterator<Draft> i = items.iterator();
        while (i.hasNext()) {
            Draft d = i.next();
            EDraftStatus status = EDraftStatus.getStatusById(d.getStatus());
            if (status != null) {
                if ((status.equals(EDraftStatus.LEGAL) && !isShowValid()) ||
                        (status.equals(EDraftStatus.CHANGED) && !isShowChanged()) ||
                        (status.equals(EDraftStatus.ANNULLED) && !isShowAnnulled()))
                    i.remove();
            }

        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(PassportInfoActivity.this, ViewerActivity.class);
        intent.putExtra("DRAFT_ID", String.valueOf(mAdapter.getItem(position).getId()));
        startActivity(intent);
    }
/*

    */
/**
     * Создаем меню для окна с информацией
     * @param menu
     * @return
     *//*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);


        getMenuInflater().inflate(R.menu.menu_info, menu);
        MenuItem btn1 = findViewById(R.id.action_valid);
        btn1.setChecked(showValid);

        MenuItem btn2 = findViewById(R.id.action_changed);
        btn2.setChecked(showValid);

        return true;
    }

    */
/**
     * Обработка выбора меню
     * @param item
     * @return
     *//*

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        MenuItem btn = findViewById(R.id.action_valid);
        CheckBox checkBox= (CheckBox) btn.getActionView();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_valid:
                //Меняем значение переменной valid
                showValid = !showValid;
                checkBox.setChecked(showValid);
                createRecycleViewOfFoundItems();
                return true;
            case R.id.action_changed:
                //Меняем значение переменной changed
                showChanged = !showChanged;
                findViewById(R.id.action_changed).setSelected(showValid);
                createRecycleViewOfFoundItems();
                return true;
            case R.id.action_annulled:
                //Меняем значение переменной annulled
                showAnnulled = !showAnnulled;
                findViewById(R.id.action_annulled).setSelected(showValid);
                createRecycleViewOfFoundItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }
*/
}