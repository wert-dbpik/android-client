package ru.wert.bazapik_mobile.organizer.folders;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.organizer.FragmentTag;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerFragment;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;

import static androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_FOLDERS;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_PRODUCT_GROUPS;

public class FoldersFragment extends Fragment implements FoldersRecViewAdapter.ItemFolderClickListener, OrganizerFragment<Item> {

    private Context orgContext;
    private OrganizerActivity orgActivity;
    @Setter private FoldersRecViewAdapter adapter;
    @Getter@Setter private RecyclerView rv;
    @Getter@Setter private List<Item> allItems;
    @Getter@Setter private List<Item> foundItems;

    @Getter@Setter private Bundle initBundle;

    private final String SAVED_STATE_BUNDLE = "saved_state_bundle";
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SEARCH_TEXT = "search_text";
    private final String UPPER_PRODUCT_GROUP_ID = "upper_product_group_id";

    private FragmentManager fm;

    @Getter private Long upperProductGroupId;
    @Setter@Getter private Integer localSelectedPosition;


    @Override
    public OrganizerRecViewAdapter getAdapter() {
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

            orgActivity.getEditTextSearch().setText(b.getString(SEARCH_TEXT));

        }
    }

    private Bundle createSaveStateBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_TEXT, orgActivity.getEditTextSearch().getText().toString());
        Parcelable listState = Objects.requireNonNull(rv.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_folders, container, false);
        this.orgActivity = (OrganizerActivity) getActivity();
        this.orgContext = getContext();
        this.fm = orgActivity.getFm();


        rv = v.findViewById(R.id.recycle_view_folders);

        initBundle = getArguments();
        if(initBundle == null) {
            upperProductGroupId = 1L;
        } else {
            upperProductGroupId = initBundle.getLong(UPPER_PRODUCT_GROUP_ID);
        }
        createRecycleViewOfFoundItems();

        if(localSelectedPosition != null)
            adapter.setSelectedPosition(localSelectedPosition);

        orgActivity.fragmentChanged(this);
        orgActivity.setCurrentFragment(FragmentTag.FOLDERS_TAG);

        adapter.setStateRestorationPolicy(PREVENT_WHEN_EMPTY);

        orgActivity.setCurrentFoldersFragment(this);
        return v;
    }


    @Override
    public void onStop() {
        super.onStop();
        onSaveInstanceState(createSaveStateBundle());
    }

    /**
     * При клике на элемент списка открывается информация об элементе
     */
    @Override
    public void onItemClick(View view, int position) {
        localSelectedPosition = position;
        Item clickedItem = adapter.getItem(position);
        if(clickedItem instanceof ProductGroup){
            //Если кликнули по верхней строке подкаталога
            if(position == 0 && upperProductGroupId != 1L) {//BACKWARD

                Long upperProductGroupId = ((ProductGroup) clickedItem).getParentId();

                String tag = "folders" + upperProductGroupId;
                FoldersFragment backwardFoldersFragment = (FoldersFragment) fm.findFragmentByTag(tag);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.organizer_fragment_container, backwardFoldersFragment);
                ft.commit();


            } else { //FORWARD
                Long upperProductGroupId = clickedItem.getId();

                Bundle bundle = new Bundle();
                bundle.putLong(UPPER_PRODUCT_GROUP_ID, upperProductGroupId);

                FoldersFragment forwardFoldersFragment = new FoldersFragment();
                forwardFoldersFragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                String tag = "folders" + upperProductGroupId;
                ft.replace(R.id.organizer_fragment_container, forwardFoldersFragment, tag);
                ft.addToBackStack("folders" + upperProductGroupId);
                ft.commit();

            }
        }
        if(clickedItem instanceof Folder){
            orgActivity.setSelectedFolder((Folder)clickedItem);
            PassportsFragment passportsFragment = orgActivity.getCurrentPassportsFragment();
            FragmentTransaction ft = orgActivity.getFm().beginTransaction();
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
            ft.replace(R.id.organizer_fragment_container, passportsFragment);
            ft.commit();
        }
    }


    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        fillRecViewWithItems(currentListWithGlobalOff(null));

        //При касании списка, поле ввода должно потерять фокус
        //чтобы наша клавиатура скрылась с экрана и мы увидели весь список
        rv.setOnTouchListener((v, event) -> {
            ((OrganizerActivity)getActivity()).getEditTextSearch().clearFocus();
            return false; //если возвращать true, то список ограничится видимой частью
        });

        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void fillRecViewWithItems(List<Item> items){
        orgActivity.runOnUiThread(()->{
            adapter = new FoldersRecViewAdapter(this, orgContext, items);
            adapter.setClickListener(FoldersFragment.this);
            rv.setAdapter(adapter);
        });
    }

    public List<Item> currentListWithGlobalOff(Item item){
//        if(adapter != null) {
//            adapter.setSelectedPosition(RecyclerView.NO_POSITION);
//            adapter.notifyDataSetChanged();
//        }
        List<Item> foundItems = new ArrayList<>();
        //Нулевая точка - исходное состояние каталога
        if(upperProductGroupId.equals(1L)) {
            List<ProductGroup> foundPG = findProductGroupChildren(upperProductGroupId);
            foundPG.sort(ThisApplication.usefulStringComparator());
            foundItems.addAll(foundPG);

        } else {//Где-то внутри каталога
            //Текущая группа
            ProductGroup currentPG = findProductGroupById(upperProductGroupId);

            List<ProductGroup> foundPG = null;
            List<Folder> foundF = null;
            if (currentPG != null) {
                //Находим входящие группы
                foundPG = findProductGroupChildren(upperProductGroupId);
                foundPG.sort(ThisApplication.usefulStringComparator());

                //Находим входящие папки
                foundF = findFoldersInProductGroup(currentPG);
                foundF.sort(ThisApplication.usefulStringComparator());
            }

            //Формируем окончательный список
            foundItems.add(currentPG);
            if(foundPG != null) foundItems.addAll(foundPG);
            if(foundF != null)  foundItems.addAll(foundF);
        }
        return foundItems;
    }

    /**
     * Здесь происходит высев подходящих под ПОИСК элементов
     * @param searchText набранный в ПОИСКе текст
     * @return List<P> список подходящих элементов
     */
    @Override //OrganizerFragment
    public List<Item> findProperItems(String searchText) {
        List<Item> foundItems = new ArrayList<>();
        for (Folder f : ALL_FOLDERS) {
            if (f.toUsefulString().toLowerCase().contains(searchText.toLowerCase()))
                foundItems.add(f);
        }
        return foundItems;
    }


    private ProductGroup findProductGroupById(Long id){
        for(ProductGroup pg: ALL_PRODUCT_GROUPS){
            if(pg.getId().equals(id))
                return pg;
        }
        return null;
    }

    private List<ProductGroup> findProductGroupChildren(Long productGroupId){
        List<ProductGroup> foundGroups = new ArrayList<>();
        for(ProductGroup pg: ALL_PRODUCT_GROUPS){
            if(pg.getParentId().equals(productGroupId))
            foundGroups.add(pg);
        }
        return foundGroups;
    }

    private List<Folder> findFoldersInProductGroup(ProductGroup productGroup){

        List<Folder> foundFolders = new ArrayList<>();
        for(Folder f: ALL_FOLDERS){
            if(f.getProductGroup().equals(productGroup))
                foundFolders.add(f);
        }
        return foundFolders;
    }

}