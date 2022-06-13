package ru.wert.bazapik_mobile.organizer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.PassportApiInterface;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.search.DraftsRecViewAdapter;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;


public class PassportsFragment extends Fragment implements DraftsRecViewAdapter.ItemDraftsClickListener, OrganizerFragment<Passport>{

    private Context context;
    private Activity activity;
    private Button btnSwipePassports;
    @Setter private DraftsRecViewAdapter<Passport> adapter;
    @Getter@Setter private RecyclerView recViewItems;
    @Getter@Setter private List<Passport> allItems;
    @Getter@Setter private List<Passport> foundItems;

    private LinearLayout selectedPosition;

    @Override
    public DraftsRecViewAdapter<Passport> getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_passports, container, false);
        this.activity = getActivity();
        this.context = getContext();
        btnSwipePassports = v.findViewById(R.id.btnSwipePassports);
        btnSwipePassports.setOnTouchListener(((OrganizerActivity)getContext()).createOnSwipeTouchListener());

        recViewItems = v.findViewById(R.id.recycle_view_passports);
        selectedPosition = v.findViewById(R.id.selected_position);

        createRecycleViewOfFoundItems();

        return v;
    }

    /**
     * При клике на элемент списка открывается информация об элементе
     */
    @Override
    public void onItemClick(View view, int position) {
//        openInfoView(position);
    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        recViewItems.setLayoutManager(new LinearLayoutManager(getContext()));

        fillRecViewWithItems();

        //При касании списка, поле ввода должно потерять фокус
        //чтобы наша клавиатура скрылась с экрана и мы увидели весь список
        recViewItems.setOnTouchListener((v, event) -> {
            ((OrganizerActivity)getActivity()).getEditTextSearch().clearFocus();
            return false; //если возвращать true, то список ограничится видимой частью
        });

        //Для красоты используем разделитель между элементами списка
        recViewItems.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

    }

    private void fillRecViewWithItems() {
        PassportApiInterface api = RetrofitClient.getInstance().getRetrofit().create(PassportApiInterface.class);
        Call<List<Passport>> call = api.getAll();
        call.enqueue(new Callback<List<Passport>>() {
            @Override
            public void onResponse(Call<List<Passport>> call, Response<List<Passport>> response) {
                if(response.isSuccessful()){
                    activity.runOnUiThread(() -> {
                        adapter = new DraftsRecViewAdapter<>(context, response.body());
                        adapter.setClickListener(PassportsFragment.this);
                        recViewItems.setAdapter(adapter);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Passport>> call, Throwable t) {
                activity.runOnUiThread(()->{
                    new WarningDialog1().show(getActivity(), "Внимание!",
                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");

                });
            }
        });

    }

    /**
     * Здесь происходит высев подходящих под ПОИСК элементов
     * @param text набранный в ПОИСКе текст
     * @return List<P> список подходящих элементов
     */
    @Override
    public List<Passport> findProperItems(String text) {
                List<Passport> foundItems = new ArrayList<>();
        for(Passport item : allItems){
            if(item.toUsefulString().toLowerCase().contains(text.toLowerCase()))
                foundItems.add(item);
        }
        return foundItems;
    }

}