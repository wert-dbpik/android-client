package ru.wert.bazapik_mobile.info;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.FilterDialog;
import ru.wert.bazapik_mobile.remark.IRemarkFragmentInteraction;
import ru.wert.bazapik_mobile.remark.InfoRemarksViewAdapter;
import ru.wert.bazapik_mobile.remark.RemarkEditorFragment;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class InfoActivity extends BaseActivity  implements
        RemarkRetrofitService.IRemarkFindByPassportId,
        InfoDraftsViewAdapter.InfoDraftClickListener,
        InfoRemarksViewAdapter.InfoRemarkClickListener,
        IRemarkFragmentInteraction {

    private static final String TAG = "+++ PassportInfoActivity +++" ;
    private LinearLayout llInfo;
    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts, rvRemarks;
    private TextView tvDrafts, tvRemarks;
    private InfoDraftsViewAdapter draftsAdapter;
    private InfoRemarksViewAdapter remarksAdapter;
    private Long passId;

    @Getter private Passport passport;
    private String decNum;

    private Bundle resumeBundle;
    private final String KEY_RECYCLER_DRAFTS_STATE = "recycler_drafts_state";
    private final String KEY_RECYCLER_REMARKS_STATE = "recycler_remarks_state";
    private final String REMARK_TEXT = "remark_text";

    private FragmentContainerView remarkContainerView;
    private RemarkEditorFragment remarkEditorFragment;

    //Все найденные элементы
    private List<Draft> foundDrafts;
    private ArrayList<String> foundDraftIdsForIntent;
    private ArrayList<String> foundRemarkIdsForIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        //Получаем Id пасспорта
        passId = Long.parseLong(getIntent().getStringExtra("PASSPORT_ID"));
        remarkContainerView = findViewById(R.id.addRemarkContainer);
        remarkContainerView.setVisibility(View.INVISIBLE);

        remarkEditorFragment = new RemarkEditorFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.addRemarkContainer, (Fragment) remarkEditorFragment, "remark_tag");
        ft.commit();

        llInfo = findViewById(R.id.llInfo); //LinearLayout для удаления надписи

        tvDecNumber = findViewById(R.id.tvDecNumber);//Децимальный номер пасспорта
        tvName = findViewById(R.id.tvName); //Наименование пасспорта
        tvDrafts = findViewById(R.id.tvDrafts);//Строка Доступные чертежи
        rvDrafts = findViewById(R.id.rvDrafts); //RecycleView


        tvRemarks = findViewById(R.id.tvRemarks);//Строка Доступные чертежи

        new Thread(()->{

            passport = ThisApplication.PASSPORT_SERVICE.findById(passId);

            if(passport != null) {
                decNum = passport.getPrefix() == null ?
                        passport.getNumber() :
                        passport.getPrefix().getName() + "." + passport.getNumber();

                runOnUiThread(() -> {
                    createRecycleViewOfFoundDrafts();

                    rvRemarks = findViewById(R.id.rvRemarks); //RecycleView
                    createRecycleViewOfFoundRemarks();

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

    private void createRecycleViewOfFoundRemarks() {
        rvRemarks.setLayoutManager(new LinearLayoutManager(this));

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<List<Remark>> call =  api.getAllByPassportId(passId);
        call.enqueue(new Callback<List<Remark>>() {
            @Override
            public void onResponse(Call<List<Remark>> call, Response<List<Remark>> response) {
                if(response.isSuccessful()) {
                    ArrayList<Remark> foundRemarks  = new ArrayList<>();
                    if(response.body() != null) foundRemarks = new ArrayList<>(response.body());

                    if (foundRemarks.isEmpty()) {
                        tvRemarks.setVisibility(View.INVISIBLE);
                    } else {
                        tvRemarks.setVisibility(View.VISIBLE);
                        if (foundRemarks.size() > 1) {
                            foundRemarks =
                                    new ArrayList<>(foundRemarks.stream()
                                            .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                                            .collect(Collectors.toList()));
                        }
                    }
//                    foundRemarkIdsForIntent = ThisApplication.convertToStringArray(new ArrayList<>(sortedList));

                    remarksAdapter = new InfoRemarksViewAdapter(InfoActivity.this, foundRemarks);
                    remarksAdapter.setClickListener(InfoActivity.this);
                    rvRemarks.setAdapter(remarksAdapter);
                } else {
                    new WarningDialog1().show(InfoActivity.this, "Внимание!", "Проблемы на линии!");
                }
            }

            @Override
            public void onFailure(Call<List<Remark>> call, Throwable t) {
                new WarningDialog1().show(InfoActivity.this, "Внимание!","Проблемы на линии!");
            }

        });

        //Для красоты используем разделитель между элементами списка
        rvRemarks.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    public void updateRemarkAdapter(){
        RemarkRetrofitService.findByPassportId(InfoActivity.this, this, passId);
        //Смотри doWhenRemarkHasBeenFoundByPassportId
        //Происходит перестроение RecyclerView
    }

    @Override
    public void doWhenRemarkHasBeenFoundByPassportId(Response<List<Remark>> response) {
        ArrayList<Remark> foundRemarks  = new ArrayList<>();
        if(response.body() != null) foundRemarks = new ArrayList<>(response.body());

        if(foundRemarks.isEmpty()) {
            tvRemarks.setVisibility(View.INVISIBLE);
        } else {
            tvRemarks.setVisibility(View.VISIBLE);
            if (foundRemarks.size() > 1) {
                foundRemarks = new ArrayList<>(foundRemarks.stream()
                        .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                        .collect(Collectors.toList()));
            }
        }
        remarksAdapter.changeListOfItems(foundRemarks);
    }

    public void changeRemark(Remark remark) {
        remarkEditorFragment.getEditor().setText(remark.getText());
        remarkEditorFragment.setChangedRemark(remark);
        remarkEditorFragment.getBtnAdd().setText(RemarkEditorFragment.sChange);
        remarkContainerView.setVisibility(View.VISIBLE);

    }

    public void deleteRemark(Remark remark) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Void> call =  api.deleteById(remark.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    findPassportById(passId).getRemarkIds().remove(remark.getId());
                    updateRemarkAdapter();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                new WarningDialog1().show(InfoActivity.this, "Внимание!", "Проблемы на линии!");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(resumeBundle == null){
            remarkContainerView.setVisibility(View.INVISIBLE);
        } else {
            String remarkText = resumeBundle.getString(REMARK_TEXT);
            if (remarkText == null || remarkText.isEmpty())
                remarkContainerView.setVisibility(View.INVISIBLE);
            else {
                remarkContainerView.setVisibility(View.VISIBLE);
                remarkEditorFragment.getEditor().setText(remarkText);
                remarkEditorFragment.getEditor().setSelection(remarkText.length());
            }

            Parcelable listDraftsState = resumeBundle.getParcelable(KEY_RECYCLER_DRAFTS_STATE);
            if (listDraftsState != null && rvDrafts != null)
                rvDrafts.getLayoutManager().onRestoreInstanceState(listDraftsState);

            Parcelable listRemarksState = resumeBundle.getParcelable(KEY_RECYCLER_REMARKS_STATE);
            if (listRemarksState != null && rvRemarks != null)
                rvRemarks.getLayoutManager().onRestoreInstanceState(listRemarksState);

            resumeBundle = null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        resumeBundle = new Bundle();
        resumeBundle.putString(REMARK_TEXT, remarkEditorFragment.getEditor().getText().toString());

        if(rvDrafts != null) {
            Parcelable listDraftsState = rvDrafts.getLayoutManager().onSaveInstanceState();
            resumeBundle.putParcelable(KEY_RECYCLER_DRAFTS_STATE, listDraftsState);
        }

        if(rvRemarks != null) {
            Parcelable listRemarksState = rvRemarks.getLayoutManager().onSaveInstanceState();
            resumeBundle.putParcelable(KEY_RECYCLER_REMARKS_STATE, listRemarksState);
        }

    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundDrafts() {

        rvDrafts.setLayoutManager(new LinearLayoutManager(this));

        DraftApiInterface api = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
        Call<List<Draft>> call =  api.getByPassportId(passId);
        call.enqueue(new Callback<List<Draft>>() {
            @Override
            public void onResponse(Call<List<Draft>> call, Response<List<Draft>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    ArrayList<Draft> foundDrafts = new ArrayList<>(response.body());
                    ThisApplication.filterList(foundDrafts); //Фильтруем
                    foundDraftIdsForIntent = ThisApplication.convertToStringArray(foundDrafts); //Для
                    draftsAdapter = new InfoDraftsViewAdapter(InfoActivity.this, foundDrafts);
                    draftsAdapter.setClickListener(InfoActivity.this);
                    rvDrafts.setAdapter(draftsAdapter);
                } else {
                    new WarningDialog1().show(InfoActivity.this, "Внимание!","Проблемы на линии!");
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
    public void onDraftRowClick(View view, int position) {
        Intent intent = new Intent(InfoActivity.this, ViewerActivity.class);
        intent.putStringArrayListExtra("DRAFTS", foundDraftIdsForIntent);
        intent.putExtra("DRAFT_ID", String.valueOf(draftsAdapter.getItem(position).getId()));
        intent.putExtra("PASSPORT_ID", String.valueOf(draftsAdapter.getItem(position).getPassport().getId()));
        startActivity(intent);
    }

    @Override
    public void onRemarkRowClick(View view, int position) {

    }


    /**
     * Создаем меню для окна с информацией
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;

    }


/**
     * Обработка выбора меню
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню


        switch (id) {

            case R.id.action_update:
                Intent updateView = new Intent(InfoActivity.this, DataLoadingActivity.class);
                startActivity(updateView);
                return true;

            case R.id.action_showFilterDialog:
                FilterDialog filterDialog = new FilterDialog(InfoActivity.this);
                filterDialog.show();
                return true;

            case R.id.action_addRemark:
                remarkEditorFragment.getEditor().setText("");
                remarkEditorFragment.getBtnAdd().setText(RemarkEditorFragment.sAdd);
                remarkContainerView.setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }

    @Override
    public void onBackPressed() {
        if(remarkContainerView.getVisibility() == View.VISIBLE) {
            remarkContainerView.setVisibility(View.INVISIBLE);
        } else
            super.onBackPressed();
    }

    @Override
    public void closeRemarkFragment() {
        try {
            InputMethodManager input = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e) {
            e.printStackTrace();
        }
        remarkContainerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Passport findPassportById(Long id){
        for(Passport p : ALL_PASSPORTS){
            if(p.getId().equals(id))
                return p;
        }
        return null;
    }


}