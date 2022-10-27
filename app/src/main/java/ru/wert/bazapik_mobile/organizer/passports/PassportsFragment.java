package ru.wert.bazapik_mobile.organizer.passports;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_DRAFTS;
import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_PASSPORTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.organizer.FragmentTag;
import ru.wert.bazapik_mobile.organizer.OrgActivityAndPassportsFragmentInteraction;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerFragment;


public class PassportsFragment extends Fragment implements PassportsRecViewAdapter.passportsClickListener, OrganizerFragment<Item> {

    private Context orgContext;
    private OrgActivityAndPassportsFragmentInteraction org;
    @Setter private PassportsRecViewAdapter adapter;
    @Getter@Setter private RecyclerView rv;
    @Getter@Setter private List<Item> allItems;
    @Getter@Setter private List<Item> foundItems;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SAVED_STATE_BUNDLE = "saved_state_bundle";
    public static final String PASSPORT = "passport";

    //В ресайклере отображаются все паспорта в базе
    @Getter@Setter private boolean global = true;
    @Setter@Getter private Integer localSelectedPosition;

    @Getter@Setter private List<Item> currentData;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        org = (OrgActivityAndPassportsFragmentInteraction) context;
    }

    @Override
    public PassportsRecViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_BUNDLE, createSaveStateBundle());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null){
            Bundle b = savedInstanceState.getBundle(SAVED_STATE_BUNDLE);

            Parcelable savedRecyclerLayoutState = b.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(rv.getLayoutManager()).onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private Bundle createSaveStateBundle(){
        Bundle bundle = new Bundle();

        Parcelable listState = Objects.requireNonNull(rv.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }

    @Override
    public void onStop() {
        super.onStop();
        onSaveInstanceState(createSaveStateBundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_passports, container, false);
        this.org = (OrganizerActivity) getActivity();
        this.orgContext = getContext();

        rv = v.findViewById(R.id.recycle_view_passports);

        createRecycleViewOfFoundPassportsAndFolders();

        org.fragmentChanged(this);
        org.setCurrentTypeFragment(FragmentTag.PASSPORT_TAG);

        currentData = new ArrayList<>(adapter.getData());

        return v;
    }

    /**
     * При клике на элемент списка открывается информация об элементе
     */
    @Override
    public void onItemClick(View view, int position) {
        localSelectedPosition = position;
        openInfoView(position);
    }


    /**
     * Открываем окно с информацией об элементе, и доступных чертежах
     * @param position
     */
    private void openInfoView(int position){
        Intent intent = new Intent(((Activity)org), InfoActivity.class);
        Passport passport = adapter.getItem(position);
        intent.putExtra(PASSPORT, passport);
        startActivity(intent);
    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundPassportsAndFolders() {

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        fillRecViewWithItems(findPassports(org.getSelectedFolder()));

        //При касании списка, поле ввода должно потерять фокус
        //чтобы наша клавиатура скрылась с экрана и мы увидели весь список
        rv.setOnTouchListener((v, event) -> {
            org.getEditTextSearch().clearFocus();
            return false; //если возвращать true, то список ограничится видимой частью
        });

        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

    }

    private List<Item> findPassports(Folder selectedFolder){
        List<Item> foundPassports = null;
        if(selectedFolder == null) {
            foundPassports = new ArrayList<>(LIST_OF_ALL_PASSPORTS);
            global = true;
        } else {
            foundPassports = findPassportsInFolder(selectedFolder);
            global = false;
        }
        return foundPassports;
    }


    public List<Item> findPassportsInFolder(Folder folder){
        Set<Passport> foundPassports = new HashSet<>();
        for(Draft d: LIST_OF_ALL_DRAFTS){
            if(d.getFolder().equals(folder))
                foundPassports.add(d.getPassport());
        }
        List<Passport> sortedList = new ArrayList<>(foundPassports);
        sortedList.sort(ThisApplication.usefulStringComparator());
        return new ArrayList<>(sortedList);
    }

    @Override //OrganizerFragment
    public void fillRecViewWithItems(List<Item> items) {
        ((Activity)org).runOnUiThread(() -> {
            adapter = new PassportsRecViewAdapter(this, orgContext, items);
            adapter.setClickListener(PassportsFragment.this);
            rv.setAdapter(adapter);
        });
    }

    /**
     * Здесь происходит высев подходящих под ПОИСК элементов
     * @param text набранный в ПОИСКе текст
     * @return List<P> список подходящих элементов
     */
    @Override
    public List<Item> findProperItems(String text) {
        List<Item> foundItems = new ArrayList<>();
        for (Item item : currentData) {
            if (item.toUsefulString().toLowerCase().contains(text.toLowerCase())){
                foundItems.add(item);
            }

        }
        foundItems.sort(ThisApplication.usefulStringComparator());
        return foundItems;
    }


}
