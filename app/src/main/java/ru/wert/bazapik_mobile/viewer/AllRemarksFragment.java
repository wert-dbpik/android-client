package ru.wert.bazapik_mobile.viewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.info.InfoRemarksViewAdapter;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AllRemarksFragment extends Fragment {

    private RecyclerView rvRemarks;
    private InfoRemarksViewAdapter remarksAdapter;
    private ViewerActivity viewerActivity;
    private Long passId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewerActivity = (ViewerActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_remarks, container, false);
        rvRemarks = view.findViewById(R.id.rvRemarks);
        passId = viewerActivity.getCurrentPassportId();
        createRecycleViewOfFoundRemarks();

        return view;
    }

    private void createRecycleViewOfFoundRemarks() {
        rvRemarks.setLayoutManager(new LinearLayoutManager(viewerActivity));

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<List<Remark>> call =  api.getAllByPassportId(passId);
        call.enqueue(new Callback<List<Remark>>() {
            @Override
            public void onResponse(Call<List<Remark>> call, Response<List<Remark>> response) {
                if(response.isSuccessful()) {
                    ArrayList<Remark> foundRemarks  = new ArrayList<>();
                    if(response.body() != null) foundRemarks = new ArrayList<>(response.body());

                        if (foundRemarks.size() > 1) {
                            foundRemarks =
                                    new ArrayList<>(foundRemarks.stream()
                                            .sorted((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()))
                                            .collect(Collectors.toList()));
                        }

                    remarksAdapter = new InfoRemarksViewAdapter(viewerActivity, foundRemarks);
                    rvRemarks.setAdapter(remarksAdapter);
                } else {
                    new WarningDialog1().show(getActivity(), "Внимание!", "Проблемы на линии!");
                }
            }

            @Override
            public void onFailure(Call<List<Remark>> call, Throwable t) {
                new WarningDialog1().show(viewerActivity, "Внимание!","Проблемы на линии!");
            }

        });

        //Для красоты используем разделитель между элементами списка
        rvRemarks.addItemDecoration(new DividerItemDecoration(viewerActivity,
                DividerItemDecoration.VERTICAL));

    }
}