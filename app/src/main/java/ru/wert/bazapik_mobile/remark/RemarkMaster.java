package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkMaster implements RemarkRetrofitService.IRemarkCreate, RemarkRetrofitService.IRemarkChange, RemarkRetrofitService.IRemarkAddPic{

    private final InfoActivity infoActivity;
    private final Long passId;

    private boolean remarksShown; //Флаг комментарии отображаются
    @Setter private int countOfRemarks; //Числовое значение количества коментариев, меняется при добавлени и удалении комментариев
    @Getter private final RemarkEditorFragment remarkEditorFragment = new RemarkEditorFragment() ;
    @Setter private Remark changedRemark;


    private RemarksAdapter remarksAdapter;
    @Getter private final LinearLayout llCommentsContainer; //Контейенер содержащий Надпись, количество комментариев и кнопку свернуть/развернуть
    @Getter private final TextView tvCountOfRemarks; //Количество комментариев, меняется при добавлени и удалении комментариев
    @Getter private final FragmentContainerView remarkContainerView;
    @Getter private final RecyclerView rvRemarks;
    private final TextView tvRemarks; //Слово Комментарии

    /**
     * Конструктор.
     * @param activity InfoActivity
     * @param passId Long
     */
    public RemarkMaster(InfoActivity activity, Long passId) {
        this.infoActivity = activity;
        this.passId = passId;

        llCommentsContainer = activity.findViewById(R.id.llCommentsContainer);
        tvCountOfRemarks = activity.findViewById(R.id.tvCountOfRemarks);
        remarkContainerView = activity.findViewById(R.id.addRemarkContainer);
        remarkContainerView.setVisibility(View.INVISIBLE);

        tvRemarks = activity.findViewById(R.id.tvNewComment); //Текст Новый комментарий
        rvRemarks = activity.findViewById(R.id.rvInfoRemarks); //RecycleView

        final ImageButton btnOpenAllRemarks = activity.findViewById(R.id.btnOpenAllRemarks);
        btnOpenAllRemarks.setOnClickListener(v->{
            if(!remarksShown) {
                showAllRemarks();
                btnOpenAllRemarks.setImageResource(R.drawable.shevron_up_white);
            }else {
                hideAllRemarks();
                btnOpenAllRemarks.setImageResource(R.drawable.shevron_down_white);
            }
            remarksShown = !remarksShown;
        });


        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.addRemarkContainer, remarkEditorFragment, "remark_tag");
        ft.commit();
    }

    public void createRecycleViewOfFoundRemarks() {
        rvRemarks.setLayoutManager(new LinearLayoutManager(infoActivity));

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<List<Remark>> call =  api.getAllByPassportId(passId);
        call.enqueue(new Callback<List<Remark>>() {
            @Override
            public void onResponse(Call<List<Remark>> call, Response<List<Remark>> response) {
                if(response.isSuccessful()) {
                    ArrayList<Remark> foundRemarks  = new ArrayList<>();
                    if(response.body() != null) foundRemarks = new ArrayList<>(response.body());

                    if (foundRemarks.isEmpty()) {
//                        hideInfoInCommentsContainer();
                    } else {
//                        showInfoInCommentsContainer();
                        if (foundRemarks.size() > 1) {
                            foundRemarks =
                                    new ArrayList<>(foundRemarks.stream()
                                            .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                                            .collect(Collectors.toList()));
                        }
                    }
//                    foundRemarkIdsForIntent = ThisApplication.convertToStringArray(new ArrayList<>(sortedList));

                    remarksAdapter = new RemarksAdapter(infoActivity, foundRemarks);
//                    remarksAdapter.setClickListener(infoActivity);
                    rvRemarks.setAdapter(remarksAdapter);
                    //Оптимизация работы
                    rvRemarks.setItemViewCacheSize(20);
                    rvRemarks.setDrawingCacheEnabled(true);
                    rvRemarks.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                } else {
                    new WarningDialog1().show(infoActivity, "Внимание!", "Проблемы на линии!");
                }
            }

            @Override
            public void onFailure(Call<List<Remark>> call, Throwable t) {
                new WarningDialog1().show(infoActivity, "Внимание!","Проблемы на линии!");
            }

        });

        //Для красоты используем разделитель между элементами списка
        rvRemarks.addItemDecoration(new DividerItemDecoration(infoActivity.getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    public void deleteRemark(Remark remark, int pos) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Void> call =  api.deleteById(remark.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    infoActivity.findPassportById(passId).getRemarkIds().remove(remark.getId());

                    remarksAdapter.notifyItemRemoved(pos);
//                    updateRemarkAdapter();
                    decreaseCountOfRemarks();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                new WarningDialog1().show(infoActivity, "Внимание!", "Проблемы на линии!");
            }
        });
    }

    /**
     * Метод настраивает редактор Комментариев под изменение
     * @param remark Remark
     */
    public void openChangeRemarkFragment(Remark remark) {
        remarkEditorFragment.getTvTitle().setText("Изменить комментарий:");
        remarkEditorFragment.getTextEditor().setText(remark.getText());
        remarkEditorFragment.getPicsAdapter().changeListOfItems(remark.getPicsInRemark());
        remarkEditorFragment.setPicsInAdapter(new ArrayList<>(remark.getPicsInRemark()));
        setChangedRemark(remark);
        remarkEditorFragment.getBtnAdd().setText(RemarkEditorFragment.sChange);
        remarkContainerView.setVisibility(View.VISIBLE);

    }


    public void updateRemarkAdapter(){
        RemarkRetrofitService.findByPassportId(infoActivity, infoActivity, passId);
        //Смотри doWhenRemarkHasBeenFoundByPassportId
        //Происходит перестроение RecyclerView
    }

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

    /**
     * Метод делает видимым контенер LinearLayout Комментарии и меняет количество комментариев
     * Вызывается после добавления или удаления комментариев
     */
    public void showInfoInCommentsContainer() {
        llCommentsContainer.setVisibility(View.VISIBLE);
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    /**
     * Метод скрывает контенер LinearLayout Комментарии и скрывает количество комментариев
     * Вызывается после удаления комментариев, когда их количество становится = 0
     */
    public void hideInfoInCommentsContainer() {
        llCommentsContainer.setVisibility(View.GONE);
        tvCountOfRemarks.setText("");
    }

    /**
     * Метод делает видимым recyclerView
     * либо создает новый, если он еще не создан
     * Вызывается при нажатии на шеврон
     */
    public void showAllRemarks(){
        if(rvRemarks.getVisibility() == View.GONE)
            rvRemarks.setVisibility(View.VISIBLE);
        else
            createRecycleViewOfFoundRemarks();
    }

    /**
     * Метод скрывает recyclerView
     * Вызывается при нажатии на шеврон
     */
    public void hideAllRemarks(){
        rvRemarks.setVisibility(View.GONE);
    }

    /**
     * Метод увеличиват количество комментариев на +1
     * И обновляет textView c количеством Комментариев
     */
    public void increaseCountOfRemarks() {
        countOfRemarks++;
        if(llCommentsContainer.getVisibility() == View.GONE) showInfoInCommentsContainer();
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    /**
     * Метод уменьшает количество комментариев на -1
     * И обновляет textView c количеством Комментариев
     */
    public void decreaseCountOfRemarks() {
        countOfRemarks--;
        if(countOfRemarks == 0) hideInfoInCommentsContainer();
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    /**
     * Метод насильно закрывает фрагмент с редактором комментарий, который появляется при нажатии на кнопку back
     * return происходит из-за Injecting to another application requires INJECT_EVENTS permission
     */
    public void closeRemarkFragment() {
        try {
            InputMethodManager input = (InputMethodManager) infoActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(infoActivity.getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e) {
            return;
        }
        remarkContainerView.setVisibility(View.INVISIBLE);
    }


    /**
     * Метод добавляет новое замечание в БД
     * Вызывается из RemarkEditorFragment
     * @param picsInAdapter
     */
    public void addRemark(List<Pic> picsInAdapter){

        Remark remark = new Remark(
                infoActivity.getPassport(),
                CURRENT_USER,
                remarkEditorFragment.getTextEditor().getText().toString(),
                ThisApplication.getCurrentTime(),
                picsInAdapter
        );

        RemarkRetrofitService.create(this, infoActivity, remark);
        //Смотри doWhenRemarkIsCreated
    }

    @Override//RemarkRetrofitService.IRemarkCreator
    public void doWhenRemarkHasBeenCreated(Response<Remark> response) {

        assert response.body() != null;
        closeRemarkFragment();
        updateRemarkAdapter();
        infoActivity.findPassportById(passId)
                .getRemarkIds().add(response.body().getId());

        increaseCountOfRemarks();

        remarkEditorFragment.clearRemarkEditor();
    }

    @Override
    public void doWhenRemarkHasBeenAddedPic(Response<Set<Pic>> response) {

    }

    /**
     * Метод изменяет замечание в БД
     * Вызывается из RemarkEditorFragment
     * @param picsInAdapter List<Pic>
     */
    public void changeRemark(List<Pic> picsInAdapter){

        changedRemark.setUser(CURRENT_USER);
        changedRemark.setText(remarkEditorFragment.getTextEditor().getText().toString());
        changedRemark.setPicsInRemark(new ArrayList<>(picsInAdapter));
        changedRemark.setCreationTime(ThisApplication.getCurrentTime());

        RemarkRetrofitService.update(this, infoActivity, changedRemark);
        //Смотри doWhenRemarkHasBeenChanged
    }

    @Override//RemarkRetrofitService.IRemarkChanger
    public void doWhenRemarkHasBeenChanged(Response<Remark> response) {
        closeRemarkFragment();
        updateRemarkAdapter();

        remarkEditorFragment.clearRemarkEditor();
    }

}
