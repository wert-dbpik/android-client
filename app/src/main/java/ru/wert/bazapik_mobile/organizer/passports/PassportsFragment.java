package ru.wert.bazapik_mobile.organizer.passports;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_DRAFTS;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.FolderApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.info.PassportInfoActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerFragment;


public class PassportsFragment extends Fragment implements PassportsRecViewAdapter.passportsClickListener, OrganizerFragment<Passport> {

    private Context context;
    private OrganizerActivity orgActivity;
    private Button btnSwipePassports;
    @Setter private PassportsRecViewAdapter adapter;
    @Getter@Setter private RecyclerView recViewItems;
    @Getter@Setter private List<Passport> allItems;
    @Getter@Setter private List<Passport> foundItems;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SEARCH_TEXT = "search_text";
    private static Bundle bundleRecyclerViewState;

    private EditText editTextSearch;


    private LinearLayout selectedPosition;

    @Override
    public PassportsRecViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_passports, container, false);
        this.orgActivity = (OrganizerActivity) getActivity();
        this.context = getContext();
        btnSwipePassports = v.findViewById(R.id.btnSwipePassports);
        btnSwipePassports.setOnTouchListener(((OrganizerActivity)getContext()).createOnSwipeTouchListener());

        editTextSearch = orgActivity.getEditTextSearch();

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
        openInfoView(position);
    }


    /**
     * Открываем окно с информацией об элементе, и доступных чертежах
     * @param position
     */
    private void openInfoView(int position){
        Intent intent = new Intent(orgActivity, PassportInfoActivity.class);
        intent.putExtra("PASSPORT_ID", String.valueOf(adapter.getItem(position).getId()));
        startActivity(intent);
    }


    /**
     * При рестарте боремся с появлением стандартной клавиатурой
     */
    @Override
    public void onResume() {
        super.onResume();
        orgActivity.getEditTextSearch().clearFocus();
        if (bundleRecyclerViewState != null) {
            editTextSearch.setText(bundleRecyclerViewState.getString(SEARCH_TEXT));
            Parcelable listState = bundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(recViewItems.getLayoutManager()).onRestoreInstanceState(listState);

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        bundleRecyclerViewState = new Bundle();
        bundleRecyclerViewState.putString(SEARCH_TEXT, editTextSearch.getText().toString());
        Parcelable listState = Objects.requireNonNull(recViewItems.getLayoutManager()).onSaveInstanceState();
        bundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

    }



    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        recViewItems.setLayoutManager(new LinearLayoutManager(getContext()));

        fillRecViewWithItems(findPassports(orgActivity.getSelectedFolder()));

        //При касании списка, поле ввода должно потерять фокус
        //чтобы наша клавиатура скрылась с экрана и мы увидели весь список
        recViewItems.setOnTouchListener((v, event) -> {
            orgActivity.getEditTextSearch().clearFocus();
            return false; //если возвращать true, то список ограничится видимой частью
        });

        //Для красоты используем разделитель между элементами списка
        recViewItems.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

    }

    private List<Passport> findPassports(Folder selectedFolder){
        List<Passport> foundPassports = null;
        if(selectedFolder == null)
            foundPassports = ALL_PASSPORTS;
        else {
            foundPassports = findPassportsInFolder(selectedFolder);
        }
        return foundPassports;
    }

    public List<Passport>  findPassportsInFolder(Folder folder){
        Set<Passport> foundPassports = new HashSet<>();
        for(Draft d: ALL_DRAFTS){
            if(d.getFolder().equals(folder))
                foundPassports.add(d.getPassport());
        }
        return new ArrayList<>(foundPassports);
    }

    public void fillRecViewWithItems(List<Passport> items) {
        orgActivity.runOnUiThread(() -> {
            adapter = new PassportsRecViewAdapter(context, items);
            adapter.setClickListener(PassportsFragment.this);
            recViewItems.setAdapter(adapter);
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
        for (Passport item : allItems) {
            if (item.toUsefulString().toLowerCase().contains(text.toLowerCase()))
                foundItems.add(item);
        }
        return foundItems;
    }

}