package ru.wert.bazapik_mobile.organizer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.ProductGroupApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.search.DraftsRecViewAdapter;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class FoldersFragment extends Fragment implements FoldersRecViewAdapter.ItemFolderClickListener, OrganizerFragment<Item>{

    private Context context;
    private Activity activity;
    private Button btnSwipeFolders;
    @Setter private FoldersRecViewAdapter adapter;
    @Getter@Setter private RecyclerView recViewItems;
    @Getter@Setter private List<Item> allItems;
    @Getter@Setter private List<Item> foundItems;

    private List<ProductGroup> allGroups;

    private LinearLayout selectedPosition;

    @Override
    public OrganizerRecViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_folders, container, false);
        this.activity = getActivity();
        this.context = getContext();
        btnSwipeFolders = v.findViewById(R.id.btnSwipeFolders);
        btnSwipeFolders.setOnTouchListener(((OrganizerActivity)getContext()).createOnSwipeTouchListener());
        recViewItems = v.findViewById(R.id.recycle_view_folders);
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

        updateView();

//        FolderApiInterface api = RetrofitClient.getInstance().getRetrofit().create(FolderApiInterface.class);
//        Call<List<Folder>> call = api.getAll();
//        call.enqueue(new Callback<List<Folder>>() {
//            @Override
//            public void onResponse(Call<List<Folder>> call, Response<List<Folder>> response) {
//                if(response.isSuccessful()){
//                    activity.runOnUiThread(() -> {
//                        adapter = new ItemRecViewAdapter<>(context, response.body());
//                        adapter.setClickListener(FoldersFragment.this);
//                        recViewItems.setAdapter(adapter);
//                    });
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Folder>> call, Throwable t) {
//                activity.runOnUiThread(()->{
//                    new WarningDialog1().show(getActivity(), "Внимание!",
//                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");
//
//                });
//            }
//        });

    }

    /**
     * Здесь происходит высев подходящих под ПОИСК элементов
     * @param searchText набранный в ПОИСКе текст
     * @return List<P> список подходящих элементов
     */
    @Override //OrganizerFragment
    public List<Item> findProperItems(String searchText){
        List<Item> foundItems = new ArrayList<>();
//        for(Folder item : allItems){
//            if(item.toUsefulString().toLowerCase().contains(searchText.toLowerCase()))
//                foundItems.add(item);
//        }
        return foundItems;
    }

    private void updateView(){
        ProductGroupApiInterface pgApi = RetrofitClient.getInstance().getRetrofit().create(ProductGroupApiInterface.class);
        Call<List<ProductGroup>> pgCall = pgApi.getAll();
        pgCall.enqueue(new Callback<List<ProductGroup>>() {
            @Override
            public void onResponse(Call<List<ProductGroup>> call, Response<List<ProductGroup>> response) {
                if(response.isSuccessful()){
                    List<Item> items = new ArrayList<>();
                    List<ProductGroup> allGroups = response.body();
                    for(ProductGroup pg: allGroups){
                        items.add((Item)pg);
                    }

                    activity.runOnUiThread(() -> {
                        adapter = new FoldersRecViewAdapter(context, items);
                        adapter.setClickListener(FoldersFragment.this);
                        recViewItems.setAdapter(adapter);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ProductGroup>> call, Throwable t) {
                activity.runOnUiThread(()->{
                    new WarningDialog1().show(getActivity(), "Внимание!",
                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");

                });
            }
        });
    }
}