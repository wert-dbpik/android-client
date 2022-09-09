package ru.wert.bazapik_mobile.info;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.FilterDialog;
import ru.wert.bazapik_mobile.remark.RemarksAdapter;
import ru.wert.bazapik_mobile.remark.RemarksEditorActivity;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.AppWarnings;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.organizer.passports.PassportsFragment.PASSPORT;
import static ru.wert.bazapik_mobile.remark.RemarksAdapter.REMARK_POSITION;

/**
 * Окно отображает свойства выбранного элемента (Passport)
 * В верхней части окна выводится наименование элемента: tvDecNumber и tvName
 * Далее доступные для элемента чертежи в rvDrafts
 * для каждого чертежа представлен его тип, стр, статус
 */
public class InfoActivity extends BaseActivity  implements
        InfoDraftsViewAdapter.InfoDraftClickListener,
        RemarksAdapter.InfoRemarkClickListener
{

    private static final String TAG = "+++ PassportInfoActivity +++" ;
    private TextView tvDecNumber, tvName;
    private TextView tvCountOfRemarks;
    private RecyclerView rvDrafts;
    private RecyclerView rvRemarks;
    private TextView tvDrafts;
    private InfoDraftsViewAdapter draftsAdapter;
    private RemarksAdapter remarksAdapter;
    private ImageButton btnOpenAllRemarks;
    private ScrollView infoScrollView;
    private LinearLayout llAwaitingCurtain;//Надпись Минуточку ..., закрывающая весь экран
    private LinearLayout awaitingPlate; //Надпись Минуточку ...
    private boolean done; //Флаг окончания процесса, исп-ся для появления надписи Минуточку ...

    private Passport passport;
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
    private List<Remark> foundRemarks;
    private ArrayList<String> foundDraftIdsForIntent;
    private boolean showRemarks;
    private int countOfRemarks; //Числовое значение количества коментариев, меняется при добавлени и удалении комментариев
    private LinearLayout llCommentsContainer; //Контейенер содержащий Надпись, количество комментариев и кнопку свернуть/развернуть

    private ActivityResultLauncher<Intent> addRemarkActivityForResultLauncher;
    private ActivityResultLauncher<Intent> changeRemarkActivityForResultLauncher;

    public static final Integer ADD_REMARK = 1;
    public static final Integer CHANGE_REMARK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        done = false; //
        showAwaitingPlateLater(llAwaitingCurtain);

        llAwaitingCurtain = findViewById(R.id.awaitingWithCurtain);
        awaitingPlate = findViewById(R.id.llAwaitingPlate);
        awaitingPlate.setVisibility(View.INVISIBLE);

        passport = getIntent().getParcelableExtra(PASSPORT);

        btnOpenAllRemarks = findViewById(R.id.btnOpenAllRemarks); //RecycleView

        infoScrollView = findViewById(R.id.infoScrollView);
        llCommentsContainer = findViewById(R.id.llRemarksTitle);

        tvDecNumber = findViewById(R.id.tvDecNumber);//Децимальный номер пасспорта
        tvName = findViewById(R.id.tvName); //Наименование пасспорта
        tvDrafts = findViewById(R.id.tvDrafts);//Строка Доступные чертежи
        rvDrafts = findViewById(R.id.rvDrafts); //RecycleView
        rvRemarks = findViewById(R.id.rvInfoRemarks); //RecycleView
        tvCountOfRemarks = findViewById(R.id.tvCountOfRemarks);

        btnOpenAllRemarks.setOnClickListener(v->{
            showRemarks = !showRemarks;
            tuneRemarksView();
        });

        registerAddRemarkActivityForResultLauncher();
        registerChangeRemarkActivityForResultLauncher();

        //Не показывать комментарии при первом открытии
        showRemarks = false;

        AsyncTask<Void, Void, Void> fillInfoActivityTask = new FillInfoActivityTask();
        fillInfoActivityTask.execute();

    }

    /**
     * Перемещает комментарии в самый верх списка
     * @param remark, Remark
     * @param pos, int
     */
    public void putRemarkInTheTop(Remark remark, int pos) {
        //Изменяем пользователя и время создания
        remark.setUser(CURRENT_USER);
        remark.setCreationTime(ThisApplication.getCurrentTime());
        //Меняем комментарий в БД
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        api.update(remark);
        Call<Remark> call = api.update(remark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if (response.isSuccessful()) {
                    swapRemarks(remark, pos);
                } else {
                    Log.e(TAG + " : deleteRemark", "Не удалось переместить комментарий в верх списка");
                    new WarningDialog1().show(InfoActivity.this, "Внимание!", "Не удалось переместить комментарий в верх списка!");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(InfoActivity.this);
            }
        });
    }

    private class FillInfoActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DraftApiInterface draftApi = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
            RemarkApiInterface remarkApi = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);

            try {
                Call<List<Draft>> findDraftsCall = draftApi.getByPassportId(passport.getId());
                foundDrafts = findDraftsCall.execute().body();

                Call<List<Remark>> findRemarksCall = remarkApi.getAllByPassportId(passport.getId());
                foundRemarks = findRemarksCall.execute().body();
                countOfRemarks = foundRemarks.size();
                if (foundRemarks.size() > 1)
                    foundRemarks =
                            new ArrayList<>(foundRemarks.stream()
                                    .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                                    .collect(Collectors.toList()));
            } catch (IOException e) {
                AppWarnings.showAlert_NoConnection(InfoActivity.this);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            initTitle();
            initDrafts();
            initRemarks();

            done = true;
            llAwaitingCurtain.setVisibility(View.GONE);
        }
        private void initTitle(){
            decNum = passport.getPrefix() == null ?
                    passport.getNumber() :
                    passport.getPrefix().getName() + "." + passport.getNumber();

            tvDecNumber.setText(decNum);
            tvName.setText(passport.getName());
            tvDrafts.setText("Доступные чертежи");
        }

        private void initDrafts(){
            rvDrafts.setLayoutManager(new LinearLayoutManager(InfoActivity.this));

            ThisApplication.filterList(foundDrafts); //Фильтруем
            foundDraftIdsForIntent = ThisApplication.convertToStringArray(new ArrayList<>(foundDrafts)); //Для
            draftsAdapter = new InfoDraftsViewAdapter(InfoActivity.this, foundDrafts);
            draftsAdapter.setClickListener(InfoActivity.this);
            rvDrafts.setAdapter(draftsAdapter);
            rvDrafts.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));

        }

        private void initRemarks(){
            rvRemarks.setLayoutManager(new LinearLayoutManager(InfoActivity.this));

            remarksAdapter = new RemarksAdapter(InfoActivity.this, foundRemarks);
            rvRemarks.setAdapter(remarksAdapter);
            rvRemarks.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));

            tuneRemarksView();
        }

    }

    private void tuneRemarksView(){
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
        if (foundRemarks.isEmpty())
            llCommentsContainer.setVisibility(View.GONE);
        else {
            llCommentsContainer.setVisibility(View.VISIBLE);
            if(showRemarks){
                rvRemarks.setVisibility(View.VISIBLE);
                btnOpenAllRemarks.setImageResource(R.drawable.shevron_up_white);
            } else {
                rvRemarks.setVisibility(View.GONE);
                btnOpenAllRemarks.setImageResource(R.drawable.shevron_down_white);
            }
        }
    }

    //ПОСЛЕ ДОБАВЛЕНИЯ КОММЕНТАРИЯ
    private void registerAddRemarkActivityForResultLauncher() {
        addRemarkActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Remark addedRemark = data.getParcelableExtra(NEW_REMARK);
                        createLog(false, String.format("Добавил комментарий id=%s для '%s'", addedRemark.getId(), passport.toUsefulString()));
                        remarksAdapter.getData().add(0, addedRemark);
                        remarksAdapter.notifyItemInserted(0);
                        remarksAdapter.notifyItemRangeChanged(0, remarksAdapter.getData().size());
                        countOfRemarks++;
                        showRemarks = true;
                        tuneRemarksView();
                    }
                });
    }

    //ПОСЛЕ ИЗМЕНЕНИЯ КОММЕНТАРИЯ
    private void registerChangeRemarkActivityForResultLauncher() {
        changeRemarkActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Remark changedRemark = data.getParcelableExtra(NEW_REMARK);
                        createLog(false, String.format("Изменил комментарий id=%s для '%s'", changedRemark.getId(), passport.toUsefulString()));
                        int changedRemarkPosition = data.getIntExtra(REMARK_POSITION, 0);

                        swapRemarks(changedRemark, changedRemarkPosition);
                    }
                });
    }

    /**
     * Перемещает remark из позиции position в самый верх списка
     * @param remark
     * @param position
     */
    private void swapRemarks(Remark remark, int position) {
        List<Remark> remarks = remarksAdapter.getData();
        if (position != 0) {
            remarks.remove(position);
            remarks.add(0, remark);
            remarksAdapter.notifyDataSetChanged();
        } else {
            remarks.set(0, remark);
            remarksAdapter.notifyItemChanged(position);
        }
        showRemarks = true;
        tuneRemarksView();
        infoScrollView.scrollTo(0, 0);
        rvRemarks.scrollTo(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        deployResumeBundle();
    }

    private void deployResumeBundle() {
        if (resumeBundle != null) {
            Parcelable listDraftsState = resumeBundle.getParcelable(KEY_RECYCLER_DRAFTS_STATE);
            if (listDraftsState != null && rvDrafts != null)
                rvDrafts.getLayoutManager().onRestoreInstanceState(listDraftsState);

            Parcelable listRemarksState = resumeBundle.getParcelable(KEY_RECYCLER_REMARKS_STATE);
            if (listRemarksState != null && rvRemarks != null)
                rvRemarks.getLayoutManager().onRestoreInstanceState(listRemarksState);

            resumeBundle = null;
        }
    }

    public void deleteRemark(Remark remark, int pos) {
        done = false;
        showAwaitingPlateLater(awaitingPlate);

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Void> call =  api.deleteById(remark.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    createLog(false, String.format("Удалил комментарий id=%s для '%s'", remark.getId(), passport.toUsefulString()));
                    done = true;
                    awaitingPlate.setVisibility(View.INVISIBLE);
                    remarksAdapter.getData().remove(pos);
                    remarksAdapter.notifyItemRemoved(pos);
                    remarksAdapter.notifyItemRangeChanged(pos, remarksAdapter.getData().size());

                    countOfRemarks--;
                    tuneRemarksView();
                } else {
                    Log.e(TAG + " : deleteRemark", "Не удалось удалить комментарий");
                    new WarningDialog1().show(InfoActivity.this, "Внимание!", "Не удалось удалить комментарий!");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(InfoActivity.this);
            }
        });
    }

    /**
     * Метод через паузу показывает просьбу подождать
     */
    private void showAwaitingPlateLater(LinearLayout awaitingLayout) {
        new Thread(()->{
            try {
                Thread.sleep(500);
                if(!done)
                    runOnUiThread(()-> awaitingLayout.setVisibility(View.VISIBLE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(resumeBundle == null) createResumeBundle();
    }

    private void createResumeBundle(){
        resumeBundle = new Bundle();

        if(rvDrafts != null) {
            Parcelable listDraftsState = rvDrafts.getLayoutManager().onSaveInstanceState();
            resumeBundle.putParcelable(KEY_RECYCLER_DRAFTS_STATE, listDraftsState);
        }

        if(rvRemarks != null && rvRemarks.getLayoutManager() != null) {
            Parcelable listRemarksState = rvRemarks.getLayoutManager().onSaveInstanceState();
            resumeBundle.putParcelable(KEY_RECYCLER_REMARKS_STATE, listRemarksState);
        }
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
                Intent remarksEditor = new Intent(InfoActivity.this, RemarksEditorActivity.class);
                remarksEditor.putExtra(TYPE_OF_REMARK_OPERATION, ADD_REMARK);
                remarksEditor.putExtra(REMARK_PASSPORT, passport);

                addRemarkActivityForResultLauncher.launch(remarksEditor);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void openChangeRemarkActivity(Remark changingRemark, int position) {
        Intent remarksEditor = new Intent(InfoActivity.this, RemarksEditorActivity.class);
        remarksEditor.putExtra(TYPE_OF_REMARK_OPERATION, CHANGE_REMARK);
        remarksEditor.putExtra(CHANGING_REMARK, changingRemark);
        remarksEditor.putExtra(REMARK_PASSPORT, passport);
        remarksEditor.putExtra(REMARK_POSITION, position);

        changeRemarkActivityForResultLauncher.launch(remarksEditor);

    }

}