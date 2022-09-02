package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
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
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.warnings.AppWarnings;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.info.InfoActivity.NEW_REMARK;

public class RemarkMaster implements RemarkRetrofitService.IRemarkChange, RemarkRetrofitService.IRemarkCreate{

    private final String TAG = "RemarkMaster";

    private final InfoActivity infoActivity;
    private final Long passId;

    private boolean remarksShown; //Флаг комментарии отображаются
    @Setter private int countOfRemarks; //Числовое значение количества коментариев, меняется при добавлени и удалении комментариев
    @Setter private Remark changedRemark;


    private RemarksAdapter remarksAdapter;
    @Getter private final LinearLayout llCommentsContainer; //Контейенер содержащий Надпись, количество комментариев и кнопку свернуть/развернуть
    @Getter private final TextView tvCountOfRemarks; //Количество комментариев, меняется при добавлени и удалении комментариев
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


    // =======================   ДОБАВЛЕНИЕ КОММЕНТАРИЯ ================

    /**
     * Метод добавляет новое замечание в БД
     * Вызывается из RemarkEditorFragment
     */
    public void createRemark(Remark remark){
        RemarkRetrofitService.create(this, infoActivity, remark);
        //Смотри doWhenRemarkIsCreated
    }

    @Override
    public void doWhenRemarkHasBeenCreated(Response<Remark> response) {
        increaseCountOfRemarks();
        updateRemarkAdapter();
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
    // =======================   ИЗМЕНЕНИЕ КОММЕНТАРИЯ ================

    /**
     * Метод изменяет замечание в БД
     * Вызывается из InfoActivity
     * @param changedRemark Remark
     */
    public void changeRemark(Remark changedRemark){
        RemarkRetrofitService.update(this, infoActivity, changedRemark);
        //Смотри doWhenRemarkHasBeenChanged
    }

    @Override//RemarkRetrofitService.IRemarkChanger
    public void doWhenRemarkHasBeenChanged(Response<Remark> response) {
        updateRemarkAdapter();
    }

    // =======================   УДАЛЕНИЕ КОММЕНТАРИЯ ================

    public void deleteRemark(Remark remark, int pos) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Void> call =  api.deleteById(remark.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    findPassportById(passId).getRemarkIds().remove(remark.getId());
                    remarksAdapter.getData().remove(pos);
                    remarksAdapter.notifyItemRemoved(pos);
                    remarksAdapter.notifyItemRangeChanged(pos, remarksAdapter.getData().size());
//                    updateRemarkAdapter();
                    decreaseCountOfRemarks();
                } else {
                    Log.e(TAG + " : deleteRemark", "Не удалось удалить комментарий");
                    new WarningDialog1().show(infoActivity, "Внимание!", "Не удалось удалить комментарий!");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(infoActivity);
            }
        });
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

    public Passport findPassportById(Long id){
        for(Passport p : ALL_PASSPORTS){
            if(p.getId().equals(id))
                return p;
        }
        return null;
    }
}
