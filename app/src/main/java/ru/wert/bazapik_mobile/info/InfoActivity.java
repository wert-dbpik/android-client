package ru.wert.bazapik_mobile.info;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class InfoActivity extends BaseActivity  implements InfoRecViewAdapter.InfoClickListener {
    private static final String TAG = "+++ PassportInfoActivity +++" ;
    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private InfoRecViewAdapter mAdapter;
    private Long passId;

    private Passport passport;
    private String decNum;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;

    //Все найденные элементы
    private List<Draft> foundDrafts;
    private ArrayList<String> foundDraftIdsForIntent;


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
        createRecycleViewOfFoundItems();
        new Thread(()->{
            passport = ThisApplication.PASSPORT_SERVICE.findById(passId);
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
                    new WarningDialog1().show(InfoActivity.this,
                            "Ошибка!", "Что-то пошло не так, вероятно потереяна связь с сервером.");
                    this.finish();
                });

            }
        }).start();


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            rvDrafts.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = rvDrafts.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        rvDrafts.setLayoutManager(new LinearLayoutManager(this));

        DraftApiInterface api = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
        Call<List<Draft>> call =  api.getByPassportId(passId);
        call.enqueue(new Callback<List<Draft>>() {
            @Override
            public void onResponse(Call<List<Draft>> call, Response<List<Draft>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    ArrayList<Draft> foundDrafts = new ArrayList<>(response.body());
                    ThisApplication.filterList(foundDrafts); //Фильтруем
                    foundDraftIdsForIntent = ThisApplication.convertToStringArray(foundDrafts);
                    mAdapter = new InfoRecViewAdapter(InfoActivity.this, foundDrafts);
                    mAdapter.setClickListener(InfoActivity.this);
                    rvDrafts.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Draft>> call, Throwable t) {
                new WarningDialog1().show(InfoActivity.this, "Внимание!","Проблемы на линии!");
            }
            
        });

        //Для красоты используем разделитель между элементами списка
        rvDrafts.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }


    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(InfoActivity.this, ViewerActivity.class);
        intent.putStringArrayListExtra("DRAFTS", foundDraftIdsForIntent);
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