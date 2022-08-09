package ru.wert.bazapik_mobile.info;

import static ru.wert.bazapik_mobile.organizer.passports.PassportsFragment.PASSPORT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import ru.wert.bazapik_mobile.remark.RemarkMaster;
import ru.wert.bazapik_mobile.remark.RemarksAdapter;
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
        RemarksAdapter.InfoRemarkClickListener
{

    private static final String TAG = "+++ PassportInfoActivity +++" ;
    private TextView tvDecNumber, tvName;
    private RecyclerView rvDrafts;
    private TextView tvDrafts;
    private InfoDraftsViewAdapter draftsAdapter;

    private Long passId;

    @Getter private Passport passport;
    private String decNum;

    public static final String REMARK_PASSPORT = "remark_passport";
    public static final String TYPE_OF_REMARK_OPERATION = "type_of_remark_operaton";
    public static final String NEW_REMARK = "new_remark";
    public static final String CHANGING_REMARK = "changing_remark";

    private Bundle resumeBundle;
    private final String KEY_RECYCLER_DRAFTS_STATE = "recycler_drafts_state";
    private final String KEY_RECYCLER_REMARKS_STATE = "recycler_remarks_state";

    //Все найденные элементы
    private List<Draft> foundDrafts;
    private ArrayList<String> foundDraftIdsForIntent;
    private ArrayList<String> foundRemarkIdsForIntent;

    private ActivityResultLauncher<Intent> addRemarkActivityForResultLauncher;
    private ActivityResultLauncher<Intent> changeRemarkActivityForResultLauncher;

    @Getter private RemarkMaster rm;
    public static final Integer ADD_REMARK = 1;
    public static final Integer CHANGE_REMARK = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        //Получаем Id пасспорта
//        passId = Long.parseLong(getIntent().getStringExtra("PASSPORT_ID"));
        passport = getIntent().getParcelableExtra(PASSPORT);
        passId = passport.getId();

        rm = new RemarkMaster(this, passId);

        tvDecNumber = findViewById(R.id.tvDecNumber);//Децимальный номер пасспорта
        tvName = findViewById(R.id.tvName); //Наименование пасспорта
        tvDrafts = findViewById(R.id.tvDrafts);//Строка Доступные чертежи
        rvDrafts = findViewById(R.id.rvDrafts); //RecycleView

        registerAddRemarkActivityForResultLauncher();
        registerChangeRemarkActivityForResultLauncher();

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
                            rm.createRemark(newRemark);
                        } else {

                        }

                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {

                    }

                });
    }

    private void registerChangeRemarkActivityForResultLauncher() {
        changeRemarkActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Remark changedRemark = data.getParcelableExtra(CHANGING_REMARK);
                            rm.changeRemark(changedRemark);
                        } else {

                        }

                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {

                    }

                });
    }

    private void createRVDrafts() {
        decNum = passport.getPrefix() == null ?
                passport.getNumber() :
                passport.getPrefix().getName() + "." + passport.getNumber();

        rm.setCountOfRemarks(passport.getRemarkIds().size());
        createRecycleViewOfFoundDrafts();

        if (passport.getRemarkIds().isEmpty())
            rm.getLlCommentsContainer().setVisibility(View.GONE);
        else
            rm.showInfoInCommentsContainer();

        tvDecNumber.setText(decNum);
        tvName.setText(passport.getName());
        tvDrafts.setText("Доступные чертежи");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumeBundle != null) {
            Parcelable listDraftsState = resumeBundle.getParcelable(KEY_RECYCLER_DRAFTS_STATE);
            if (listDraftsState != null && rvDrafts != null)
                rvDrafts.getLayoutManager().onRestoreInstanceState(listDraftsState);

            Parcelable listRemarksState = resumeBundle.getParcelable(KEY_RECYCLER_REMARKS_STATE);
            if (listRemarksState != null && rm.getRvRemarks() != null)
                rm.getRvRemarks().getLayoutManager().onRestoreInstanceState(listRemarksState);

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
                Intent remarksEditor = new Intent(InfoActivity.this, RemarksEditor.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt(TYPE_OF_REMARK_OPERATION, ADD_REMARK);
                remarksEditor.putExtra(TYPE_OF_REMARK_OPERATION, ADD_REMARK);
                remarksEditor.putExtra(REMARK_PASSPORT, passport);

                addRemarkActivityForResultLauncher.launch(remarksEditor);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void openChangeRemarkActivity(Remark changingRemark) {
        Intent remarksEditor = new Intent(InfoActivity.this, RemarksEditor.class);
        remarksEditor.putExtra(TYPE_OF_REMARK_OPERATION, CHANGE_REMARK);
        remarksEditor.putExtra(CHANGING_REMARK, changingRemark);
        remarksEditor.putExtra(REMARK_PASSPORT, passport);

        changeRemarkActivityForResultLauncher.launch(remarksEditor);

    }

    @Override
    public void doWhenRemarkHasBeenFoundByPassportId(Response<List<Remark>> response) {
        rm.doWhenRemarkHasBeenFoundByPassportId(response);
    }


}