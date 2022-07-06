package ru.wert.bazapik_mobile.info;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.LoginActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkFragment extends Fragment {

    @Getter private EditText editor;
    private Button btnAdd;

    private String TAG = "RemarkFragment";

    private IRemarkFragmentInteraction viewInteraction;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewInteraction = (IRemarkFragmentInteraction) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.fragment_remark, container, false);

        editor = view.findViewById(R.id.etTextRemark);
        btnAdd = view.findViewById(R.id.btnAddRemark);
        btnAdd.setOnClickListener(v->{
            addRemark();
        });

        return view;
    }

    private void addRemark(){
        //Время
        Date date = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Remark remark = new Remark(
                viewInteraction.getPassport(),
                CURRENT_USER,
                editor.getText().toString(),
                df.format(date)
        );

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.create(remark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    viewInteraction.closeRemarkFragment();
                    viewInteraction.updateRemarkAdapter();
                } else {
                    Log.d(TAG, String.format("Не удалось сохранить запись, %s", response.message()));
                    new WarningDialog1().show(getActivity(), "Ошибка!","Не удалось сохранить запись");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                Log.d(TAG, String.format("Не удалось сохранить запись, %s", t.getMessage()));
                new WarningDialog1().show(getActivity(), "Ошибка!", "Не удалось сохранить запись");
            }
        });

    }
}