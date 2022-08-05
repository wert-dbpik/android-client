package ru.wert.bazapik_mobile.info;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.FilterDialog;
import ru.wert.bazapik_mobile.remark.IRemarkFragmentInteraction;
import ru.wert.bazapik_mobile.remark.RemarksAdapter;
import ru.wert.bazapik_mobile.remark.RemarkMaster;
import ru.wert.bazapik_mobile.remark.RemarksEditor;
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
        RemarksAdapter.InfoRemarkClickListener,
        IRemarkFragmentInteraction {

    private static final String TAG = "+++ PassportInfoActivity +++" ;
    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private InfoDraftsViewAdapter draftsAdapter;

    private Long passId;

    @Getter private Passport passport;
    private String decNum;

    public static final String REMARK_PASSPORT = "remark_passport";
    public static final String NEW_REMARK = "new_remark";

    private Bundle resumeBundle;
    private final String KEY_RECYCLER_DRAFTS_STATE = "recycler_drafts_state";
    private final String KEY_RECYCLER_REMARKS_STATE = "recycler_remarks_state";
    private final String EDITOR_IS_OPEN = "editor_is_open";
    private final String REMARK_TEXT = "remark_text";
    
    //Все найденные элементы
    private List<Draft> foundDrafts;
    private ArrayList<String> foundDraftIdsForIntent;
    private ArrayList<String> foundRemarkIdsForIntent;

    private ActivityResultLauncher<Intent> addRemarkActivityForResultLauncher;
    
    @Getter private RemarkMaster rm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //Получаем Id пасспорта
        passId = Long.parseLong(getIntent().getStringExtra("PASSPORT_ID"));

        rm = new RemarkMaster(this, passId);

        tvDecNumber = findViewById(R.id.tvDecNumber);//Децимальный номер пасспорта
        tvName = findViewById(R.id.tvName); //Наименование пасспорта
        tvDrafts = findViewById(R.id.tvDrafts);//Строка Доступные чертежи
        rvDrafts = findViewById(R.id.rvDrafts); //RecycleView

        registerAddRemarkActivityForResultLauncher();

        createRVDrafts();

    }

    private void registerAddRemarkActivityForResultLauncher() {
        addRemarkActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Remark newRemark = data.getParcelableExtra(NEW_REMARK);
                                    rm.createRemarkNew(newRemark);
                        } else {

                        }

                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {

                    }

                });
    }

    private void createRVDrafts() {
        new Thread(()->{

            passport = ThisApplication.PASSPORT_SERVICE.findById(passId);

            if(passport != null) {
                decNum = passport.getPrefix() == null ?
                        passport.getNumber() :
                        passport.getPrefix().getName() + "." + passport.getNumber();

                rm.setCountOfRemarks(passport.getRemarkIds().size());
                runOnUiThread(() -> {
                    createRecycleViewOfFoundDrafts();

                    if(passport.getRemarkIds().isEmpty())
                        rm.getLlCommentsContainer().setVisibility(View.GONE);
                    else
                        showInfoInCommentsContainer();

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
        rm.getRemarkContainerView().clearAnimation();
        if(resumeBundle == null){
            rm.getRemarkContainerView().setVisibility(View.INVISIBLE);
        } else {
            Parcelable listDraftsState = resumeBundle.getParcelable(KEY_RECYCLER_DRAFTS_STATE);
            if (listDraftsState != null && rvDrafts != null)
                rvDrafts.getLayoutManager().onRestoreInstanceState(listDraftsState);

            Parcelable listRemarksState = resumeBundle.getParcelable(KEY_RECYCLER_REMARKS_STATE);
            if (listRemarksState != null && rm.getRvRemarks() != null)
                rm.getRvRemarks().getLayoutManager().onRestoreInstanceState(listRemarksState);

            if(resumeBundle.getInt(EDITOR_IS_OPEN) == View.INVISIBLE) {
                rm.getRemarkContainerView().setVisibility(View.INVISIBLE);
                new Thread(() -> {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                }).start();
            }

            resumeBundle = null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        createResumeBundle();

    }

    @Override
    protected void onStop() {
        super.onStop();
        createResumeBundle();

    }

    private void createResumeBundle(){
        resumeBundle = new Bundle();
        resumeBundle.putString(REMARK_TEXT, rm.getRemarkEditorFragment().getTextEditor().getText().toString());
        resumeBundle.putInt(EDITOR_IS_OPEN, rm.getRemarkContainerView().getVisibility());

        if(rvDrafts != null) {
            Parcelable listDraftsState = rvDrafts.getLayoutManager().onSaveInstanceState();
            resumeBundle.putParcelable(KEY_RECYCLER_DRAFTS_STATE, listDraftsState);
        }

        if(rm.getRvRemarks() != null && rm.getRvRemarks().getLayoutManager() != null) {
            Parcelable listRemarksState = rm.getRvRemarks().getLayoutManager().onSaveInstanceState();
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
                if(rm.getRvRemarks().getLayoutManager() == null)
                    rm.createRecycleViewOfFoundRemarks();
                rm.getRemarkEditorFragment().clearRemarkEditor();
                rm.getRemarkEditorFragment().getTvTitle().setText("Новый комментарий:");
                rm.getRemarkEditorFragment().getTextEditor().setText("");
                rm.getRemarkEditorFragment().getBtnAdd().setText(rm.getRemarkEditorFragment().sAdd);
                rm.getRemarkContainerView().setVisibility(View.VISIBLE);
                return true;

            case R.id.action_addRemark_new:
                Intent remarksEditor = new Intent(InfoActivity.this, RemarksEditor.class);
                remarksEditor.putExtra(REMARK_PASSPORT, passport);
                addRemarkActivityForResultLauncher.launch(remarksEditor);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(rm.getRemarkContainerView().getVisibility() == View.VISIBLE) {
            rm.getRemarkContainerView().setVisibility(View.INVISIBLE);
        } else
            super.onBackPressed();
    }


    @Override
    public Passport findPassportById(Long id){
        for(Passport p : ALL_PASSPORTS){
            if(p.getId().equals(id))
                return p;
        }
        return null;
    }

    @Override
    public FragmentContainerView getRemarkContainerView() {
        return rm.getRemarkContainerView();
    }

    @Override
    public void closeRemarkFragment() {
        rm.closeRemarkFragment();
    }

    @Override
    public void updateRemarkAdapter() {
        rm.updateRemarkAdapter();
    }

    @Override
    public void increaseCountOfRemarks() {
        rm.increaseCountOfRemarks();
    }

    @Override
    public void decreaseCountOfRemarks() {
        rm.decreaseCountOfRemarks();
    }

    @Override
    public void showInfoInCommentsContainer() {
        rm.showInfoInCommentsContainer();
    }

    @Override
    public void doWhenRemarkHasBeenFoundByPassportId(Response<List<Remark>> response) {
        rm.doWhenRemarkHasBeenFoundByPassportId(response);
    }
}