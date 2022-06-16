package ru.wert.bazapik_mobile.organizer.folders;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
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
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.organizer.FragmentTag;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerFragment;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_FOLDERS;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_PRODUCT_GROUPS;

public class FoldersFragment extends Fragment implements FoldersRecViewAdapter.ItemFolderClickListener, OrganizerFragment<Item> {

    private Context orgContext;
    private OrganizerActivity orgActivity;
    @Setter private FoldersRecViewAdapter adapter;
    @Getter@Setter private RecyclerView recViewItems;
    @Getter@Setter private List<Item> allItems;
    @Getter@Setter private List<Item> foundItems;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SEARCH_TEXT = "search_text";
    private final String PRODUCT_GROUP_ID = "product_group_id";
    private static Bundle bundleRecyclerViewState;

    @Getter private Long currentProductGroupId;

    @Override
    public OrganizerRecViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_folders, container, false);
        this.orgActivity = (OrganizerActivity) getActivity();
        this.orgContext = getContext();

        recViewItems = v.findViewById(R.id.recycle_view_folders);

        currentProductGroupId = 1L;
        createRecycleViewOfFoundItems();

        orgActivity.fragmentChanged(this);
        orgActivity.setCurrentFragment(FragmentTag.PASSPORT_TAG);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        bundleRecyclerViewState = new Bundle();
        bundleRecyclerViewState.putString(SEARCH_TEXT, orgActivity.getEditTextSearch().getText().toString());
        bundleRecyclerViewState.putString(PRODUCT_GROUP_ID, String.valueOf(currentProductGroupId));
        Parcelable listState = Objects.requireNonNull(recViewItems.getLayoutManager()).onSaveInstanceState();
        bundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        orgActivity.setCurrentFragment(FragmentTag.PASSPORT_TAG);
        orgActivity.fragmentChanged(this);
        orgActivity.getEditTextSearch().clearFocus();
        if (bundleRecyclerViewState != null) {
            orgActivity.getEditTextSearch().setText(bundleRecyclerViewState.getString(SEARCH_TEXT));
            currentProductGroupId = Long.valueOf(bundleRecyclerViewState.getString(PRODUCT_GROUP_ID));
            Parcelable listState = bundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(recViewItems.getLayoutManager()).onRestoreInstanceState(listState);
        }

        createRecycleViewOfFoundItems();

    }

    /**
     * При клике на элемент списка открывается информация об элементе
     */
    @Override
    public void onItemClick(View view, int position) {
        Item clickedItem = adapter.getItem(position);
        if(clickedItem instanceof ProductGroup){
            if(position == 0 && currentProductGroupId != 1L) {
                currentProductGroupId = ((ProductGroup) clickedItem).getParentId();
                fillRecViewWithItems(currentListWithGlobalOff());
            } else {
                currentProductGroupId = clickedItem.getId();
                fillRecViewWithItems(currentListWithGlobalOff());
            }
        }
        if(clickedItem instanceof Folder){
            orgActivity.setSelectedFolder((Folder)clickedItem);
            PassportsFragment passportsFragment = orgActivity.getPassportsFragment();
            FragmentTransaction ft = orgActivity.getFm().beginTransaction();
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
            ft.replace(R.id.organizer_fragment_container, passportsFragment, "passports_tag");
            ft.commit();
        }
    }

    private void openPassportFragment(){

    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        recViewItems.setLayoutManager(new LinearLayoutManager(getContext()));


        fillRecViewWithItems(currentListWithGlobalOff());

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

    public void fillRecViewWithItems(List<Item> items){
        orgActivity.runOnUiThread(()->{
            adapter = new FoldersRecViewAdapter(this, orgContext, items);
            adapter.setClickListener(FoldersFragment.this);
            recViewItems.setAdapter(adapter);
        });
    }

    public List<Item> currentListWithGlobalOff(){
        List<Item> foundItems = new ArrayList<>();
        if(currentProductGroupId.equals(1L)) {
            List<ProductGroup> foundPG = findProductGroupChildren(currentProductGroupId);
            foundPG.sort(ThisApplication.usefulStringComparator());
            foundItems.addAll(foundPG);
        } else {
            //Текущая группа
            ProductGroup currentPG = findProductGroupById(currentProductGroupId);

            List<ProductGroup> foundPG = null;
            List<Folder> foundF = null;
            if (currentPG != null) {
                //Находим входящие группы
                foundPG = findProductGroupChildren(currentProductGroupId);
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