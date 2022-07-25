package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.fragment.app.Fragment;
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
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class RemarkMaster {

    private final InfoActivity infoActivity;
    private final Long passId;

    private boolean remarksShown; //Флаг комментарии отображаются
    @Setter private int countOfRemarks; //Числовое значение количества коментариев, меняется при добавлени и удалении комментариев
    @Getter private final RemarkEditorFragment remarkEditorFragment = new RemarkEditorFragment() ;

    private InfoRemarksViewAdapter remarksAdapter;
    @Getter private final LinearLayout llCommentsContainer; //Контейенер содержащий Надпись, количество комментариев и кнопку свернуть/развернуть
    @Getter private final TextView tvCountOfRemarks; //Количество комментариев, меняется при добавлени и удалении комментариев
    @Getter private final FragmentContainerView remarkContainerView;
    @Getter private final RecyclerView rvRemarks;
    private final TextView tvRemarks;

    /**
     * Конструктор.
     * @param infoActivity InfoActivity
     * @param passId Long
     */
    public RemarkMaster(InfoActivity infoActivity, Long passId) {
        this.infoActivity = infoActivity;
        this.passId = passId;

        llCommentsContainer = infoActivity.findViewById(R.id.llCommentsContainer);
        tvCountOfRemarks = infoActivity.findViewById(R.id.tvCountOfRemarks);
        remarkContainerView = infoActivity.findViewById(R.id.addRemarkContainer);
        remarkContainerView.setVisibility(View.INVISIBLE);

        tvRemarks = infoActivity.findViewById(R.id.tvNewComment); //Текст Новый комментарий
        rvRemarks = infoActivity.findViewById(R.id.rvRemarks); //RecycleView

        final ImageButton btnOpenAllRemarks = infoActivity.findViewById(R.id.btnOpenAllRemarks);
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

        FragmentManager fragmentManager = infoActivity.getSupportFragmentManager();
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
                        hideInfoInCommentsContainer();
                    } else {
                        showInfoInCommentsContainer();
                        if (foundRemarks.size() > 1) {
                            foundRemarks =
                                    new ArrayList<>(foundRemarks.stream()
                                            .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                                            .collect(Collectors.toList()));
                        }
                    }
//                    foundRemarkIdsForIntent = ThisApplication.convertToStringArray(new ArrayList<>(sortedList));

                    remarksAdapter = new InfoRemarksViewAdapter(infoActivity, foundRemarks);
                    remarksAdapter.setClickListener(infoActivity);
                    rvRemarks.setAdapter(remarksAdapter);
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

    public void deleteRemark(Remark remark) {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Void> call =  api.deleteById(remark.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    infoActivity.findPassportById(passId).getRemarkIds().remove(remark.getId());
                    updateRemarkAdapter();
                    decreaseCountOfRemarks();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                new WarningDialog1().show(infoActivity, "Внимание!", "Проблемы на линии!");
            }
        });
    }

    public void openChangeRemarkFragment(Remark remark) {
        remarkEditorFragment.getTvTitle().setText("Изменить комментарий:");
        remarkEditorFragment.getTextEditor().setText(remark.getText());
        remarkEditorFragment.getPicsAdapter().changeListOfItems(remark.getPicsInRemark());
        remarkEditorFragment.setPicsInAdapter(new ArrayList<>(remark.getPicsInRemark()));
        remarkEditorFragment.setChangedRemark(remark);
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

    public void showInfoInCommentsContainer() {
        llCommentsContainer.setVisibility(View.VISIBLE);
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    public void hideInfoInCommentsContainer() {
        llCommentsContainer.setVisibility(View.GONE);
        tvCountOfRemarks.setText("");
    }

    public void showAllRemarks(){
        if(rvRemarks.getVisibility() == View.GONE)
            rvRemarks.setVisibility(View.VISIBLE);
        else
            createRecycleViewOfFoundRemarks();
    }

    public void hideAllRemarks(){
        rvRemarks.setVisibility(View.GONE);
    }

    public void increaseCountOfRemarks() {
        countOfRemarks++;
        if(llCommentsContainer.getVisibility() == View.GONE) showInfoInCommentsContainer();
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    public void decreaseCountOfRemarks() {
        countOfRemarks--;
        if(countOfRemarks == 0) hideInfoInCommentsContainer();
        tvCountOfRemarks.setText(String.valueOf(countOfRemarks));
    }

    public void closeRemarkFragment() {
        try {
            InputMethodManager input = (InputMethodManager) infoActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(infoActivity.getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e) {
            e.printStackTrace();
        }
        remarkContainerView.setVisibility(View.INVISIBLE);
    }


}
