package ru.wert.tubus_mobile.organizer.passports;

import static ru.wert.tubus_mobile.ThisApplication.LIST_OF_ALL_DRAFTS;
import static ru.wert.tubus_mobile.ThisApplication.LIST_OF_ALL_PASSPORTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.data.models.Passport;
import ru.wert.tubus_mobile.info.InfoActivity;
import ru.wert.tubus_mobile.organizer.FragmentTag;
import ru.wert.tubus_mobile.organizer.OrgActivityAndPassportsFragmentInteraction;
import ru.wert.tubus_mobile.organizer.OrganizerActivity;
import ru.wert.tubus_mobile.organizer.OrganizerFragment;

/**
 * Фрагмент для отображения и управления списком паспортов.
 * Реализует интерфейсы для взаимодействия с элементами списка и организатором.
 */
public class PassportsFragment extends Fragment implements
        PassportsRecViewAdapter.passportsClickListener,
        OrganizerFragment<Item> {

    // Константы для сохранения состояния
    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String SAVED_STATE_BUNDLE = "saved_state_bundle";
    public static final String PASSPORT = "passport";

    private Context orgContext;
    private OrgActivityAndPassportsFragmentInteraction org;

    @Setter private PassportsRecViewAdapter adapter;
    @Getter @Setter private RecyclerView rv;
    @Getter @Setter private List<Item> allItems;
    @Getter @Setter private List<Item> foundItems;

    // Флаг, указывающий, отображаются ли все паспорта (true) или только из выбранной папки (false)
    @Getter @Setter private boolean global = true;
    @Setter @Getter private Integer localSelectedPosition;
    @Getter @Setter private List<Item> currentData;

    // ======================= Жизненный цикл фрагмента =======================

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Проверяем, что активность реализует необходимый интерфейс
        if (context instanceof OrgActivityAndPassportsFragmentInteraction) {
            org = (OrgActivityAndPassportsFragmentInteraction) context;
        } else {
            throw new RuntimeException(context + " must implement OrgActivityAndPassportsFragmentInteraction");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passports, container, false);

        // Инициализация компонентов
        this.org = (OrganizerActivity) getActivity();
        this.orgContext = getContext();
        rv = view.findViewById(R.id.recycle_view_passports);

        // Создаем и настраиваем RecyclerView
        createRecycleViewOfFoundPassportsAndFolders();

        // Уведомляем активность об изменении фрагмента
        if (org != null) {
            org.fragmentChanged(this);
            org.setCurrentTypeFragment(FragmentTag.PASSPORT_TAG);
        }

        // Инициализируем текущие данные
        currentData = adapter != null ? new ArrayList<>(adapter.getData()) : new ArrayList<>();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_BUNDLE, createSaveStateBundle());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && rv != null) {
            Bundle b = savedInstanceState.getBundle(SAVED_STATE_BUNDLE);
            if (b != null) {
                Parcelable savedRecyclerLayoutState = b.getParcelable(KEY_RECYCLER_STATE);
                if (rv.getLayoutManager() != null && savedRecyclerLayoutState != null) {
                    rv.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        onSaveInstanceState(createSaveStateBundle());
    }

    // ======================= Методы работы с состоянием =======================

    /**
     * Создает Bundle для сохранения текущего состояния фрагмента
     * @return Bundle с сохраненным состоянием
     */
    private Bundle createSaveStateBundle() {
        Bundle bundle = new Bundle();
        if (rv != null && rv.getLayoutManager() != null) {
            Parcelable listState = rv.getLayoutManager().onSaveInstanceState();
            bundle.putParcelable(KEY_RECYCLER_STATE, listState);
        }
        return bundle;
    }

    // ======================= Методы работы с RecyclerView =======================

    /**
     * Создает и настраивает RecyclerView для отображения паспортов и папок
     */
    private void createRecycleViewOfFoundPassportsAndFolders() {
        if (rv == null || getContext() == null) return;

        // Настройка LayoutManager
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Заполнение RecyclerView данными
        List<Item> items = findPassports(org != null ? org.getSelectedFolder() : null);
        fillRecViewWithItems(items);

        // Обработка касания списка (скрытие клавиатуры)
        rv.setOnTouchListener((v, event) -> {
            if (org != null && org.getEditTextSearch() != null) {
                org.getEditTextSearch().clearFocus();
            }
            return false;
        });

        // Добавление разделителей между элементами
        rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * Заполняет RecyclerView элементами
     * @param items список элементов для отображения
     */
    @Override
    public void fillRecViewWithItems(List<Item> items) {
        if (org == null || orgContext == null) return;

        ((Activity) org).runOnUiThread(() -> {
            adapter = new PassportsRecViewAdapter(this, orgContext, items != null ? items : Collections.emptyList());
            adapter.setClickListener(this);
            rv.setAdapter(adapter);
        });
    }

    // ======================= Методы поиска и фильтрации =======================

    /**
     * Находит паспорта в зависимости от выбранной папки
     * @param selectedFolder папка для поиска (null - поиск по всем паспортам)
     * @return список найденных элементов (никогда не null)
     */
    private List<Item> findPassports(@Nullable Folder selectedFolder) {
        if (selectedFolder == null) {
            global = true;
            // Возвращаем копию общего списка с защитой от null
            return new ArrayList<>(LIST_OF_ALL_PASSPORTS != null ?
                    LIST_OF_ALL_PASSPORTS :
                    Collections.emptyList());
        } else {
            global = false;
            List<Item> foundInFolder = findPassportsInFolder(selectedFolder);
            return foundInFolder != null ? foundInFolder : Collections.emptyList();
        }
    }

    /**
     * Находит все паспорта в указанной папке
     * @param folder папка для поиска
     * @return отсортированный список паспортов (никогда не null)
     */
    public List<Item> findPassportsInFolder(Folder folder) {
        if (folder == null || LIST_OF_ALL_DRAFTS == null) {
            return Collections.emptyList();
        }

        Set<Passport> foundPassports = new HashSet<>();
        for (Draft draft : LIST_OF_ALL_DRAFTS) {
            if (draft != null && folder.equals(draft.getFolder())) {
                foundPassports.add(draft.getPassport());
            }
        }

        List<Passport> sortedList = new ArrayList<>(foundPassports);
        sortedList.sort(ThisApplication.usefulStringComparator());
        return new ArrayList<>(sortedList);
    }

    /**
     * Фильтрует элементы по тексту поиска
     * @param text текст для поиска
     * @return список подходящих элементов (никогда не null)
     */
    @Override
    public List<Item> findProperItems(String text) {
        if (text == null || currentData == null) {
            return Collections.emptyList();
        }

        List<Item> foundItems = new ArrayList<>();
        String searchText = text.toLowerCase();

        for (Item item : currentData) {
            if (item != null && item.toUsefulString().toLowerCase().contains(searchText)) {
                foundItems.add(item);
            }
        }

        foundItems.sort(ThisApplication.usefulStringComparator());
        return foundItems;
    }

    // ======================= Обработка кликов =======================

    /**
     * Обрабатывает клик по элементу списка
     * @param view нажатое представление
     * @param position позиция элемента
     */
    @Override
    public void onItemClick(View view, int position) {
        if (adapter == null) return;

        localSelectedPosition = position;
        openInfoView(position);
    }

    /**
     * Открывает окно с информацией о паспорте
     * @param position позиция паспорта в списке
     */
    private void openInfoView(int position) {
        if (org == null || adapter == null || position < 0 || position >= adapter.getItemCount()) {
            return;
        }

        Passport passport = adapter.getItem(position);
        if (passport != null) {
            Intent intent = new Intent(((Activity) org), InfoActivity.class);
            intent.putExtra(PASSPORT, passport);
            startActivity(intent);
        }
    }

    // ======================= Геттеры =======================

    @Override
    public PassportsRecViewAdapter getAdapter() {
        return adapter;
    }
}
