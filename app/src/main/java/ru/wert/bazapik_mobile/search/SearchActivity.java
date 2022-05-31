package ru.wert.bazapik_mobile.search;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.wert.bazapik_mobile.LoginActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.VersionAndroid;
import ru.wert.bazapik_mobile.data.servicesREST.VersionAndroidService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.info.PassportInfoActivity;
import ru.wert.bazapik_mobile.keyboards.EngKeyboard;
import ru.wert.bazapik_mobile.keyboards.KeyboardSwitcher;
import ru.wert.bazapik_mobile.keyboards.MyKeyboard;
import ru.wert.bazapik_mobile.keyboards.NumKeyboard;
import ru.wert.bazapik_mobile.keyboards.RuKeyboard;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.settings.SettingsActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.APP_VERSION_NOTIFICATION_SHOWN;

/**
 * Окно поиска чертежа.
 * Состоит из
 * 1) текстового поля поиска - mEditTextSearch
 * 2) Списка найденных элементов (Passport) - mRecViewItems
 * 3) Всплывающей клавиатуры - keyboardView
 *
 */
public class SearchActivity extends BaseActivity implements ItemRecViewAdapter.ItemClickListener, KeyboardSwitcher {
    private static final String TAG = "+++ SearchActivity +++";

    private ItemRecViewAdapter<Passport> mAdapter;
    private RecyclerView mRecViewItems;
    private List<Passport> allItems;
    private List<Passport> foundItems;
    private EditText mEditTextSearch;
    private LinearLayout mSelectedPosition;
    private FragmentContainerView keyboardContainer;
    private int oldOrientation;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SEARCH_TEXT = "search_text";
    private static Bundle mBundleRecyclerViewState;

    public static final int NUM_KEYBOARD = 0;
    public static final int RU_KEYBOARD = 1;
    public static final int ENG_KEYBOARD = 2;

    private final List<Fragment> keyboards = new ArrayList<>();
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        keyboardContainer = findViewById(R.id.keyboard_container);
        mEditTextSearch = findViewById(R.id.edit_text_search);
        mRecViewItems = findViewById(R.id.recycle_view_items);
        mSelectedPosition = findViewById(R.id.selected_position);

        createKeyboards();
        createSearchEditText();
        createRecycleViewOfFoundItems();

        new Thread(()->{
            List<VersionAndroid> alVersions = VersionAndroidService.getInstance().findAll();
            APPLICATION_VERSION_AVAILABLE = alVersions.get(alVersions.size()-1).getName();
            if(APPLICATION_VERSION_AVAILABLE.compareTo(ThisApplication.APPLICATION_VERSION) > 0 &&
                    !APP_VERSION_NOTIFICATION_SHOWN) {

                APP_VERSION_NOTIFICATION_SHOWN = true;
                runOnUiThread(() -> {
                    new Warning1().show(SearchActivity.this,
                            "Внимание!",
                            "Доступна новая версия " + APPLICATION_VERSION_AVAILABLE);
                });
            }
        }).start();

    }

    /**
     * При рестарте боремся с появлением стандартной клавиатурой
     */
    @Override
    protected void onResume() {
        super.onResume();
        mEditTextSearch.clearFocus();
        if (mBundleRecyclerViewState != null) {
            mEditTextSearch.setText(mBundleRecyclerViewState.getString(SEARCH_TEXT));
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(mRecViewItems.getLayoutManager()).onRestoreInstanceState(listState);

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        mBundleRecyclerViewState.putString(SEARCH_TEXT, mEditTextSearch.getText().toString());
        Parcelable listState = Objects.requireNonNull(mRecViewItems.getLayoutManager()).onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

    }

    /**
     * Создаем нашу нестандартную клавиатуру,
     * состояющую из ЦИФРОВОЙ и ТЕКСТОВОЙ клавиатур
     */
    private void createKeyboards() {

        NumKeyboard numberKeyboard = new NumKeyboard();
        numberKeyboard.setKeyboardSwitcher(this);
        numberKeyboard.setEditTextSearch(mEditTextSearch);
        keyboards.add(NUM_KEYBOARD, numberKeyboard);

        RuKeyboard ruKeyboard = new RuKeyboard();
        ruKeyboard.setKeyboardSwitcher(this);
        ruKeyboard.setEditTextSearch(mEditTextSearch);
        keyboards.add(RU_KEYBOARD, ruKeyboard);

        EngKeyboard engKeyboard = new EngKeyboard();
        engKeyboard.setKeyboardSwitcher(this);
        engKeyboard.setEditTextSearch(mEditTextSearch);
        keyboards.add(ENG_KEYBOARD, engKeyboard);

        switchKeyboardTo(NUM_KEYBOARD);

    }

    @Override
    public void switchKeyboardTo(int keyboard) {
        MyKeyboard myKeyboard = (MyKeyboard) keyboards.get(keyboard);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.keyboard_container, (Fragment) myKeyboard);
        ft.commit();

    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        mRecViewItems.setLayoutManager(new LinearLayoutManager(this));

        fillRecViewWithItems();

        //При касании списка, поле ввода должно потерять фокус
        //чтобы наша клавиатура скрылась с экрана и мы увидели весь список
        mRecViewItems.setOnTouchListener((v, event) -> {
            mEditTextSearch.clearFocus();
            return false; //если возвращать true, то список ограничится видимой частью
        });

        //Для красоты используем разделитель между элементами списка
        mRecViewItems.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    private void fillRecViewWithItems() {
        new Thread(() -> {
            try {
                allItems = (List<Passport>) ThisApplication.PASSPORT_SERVICE.findAll();
                List<Passport> items = new ArrayList<>();
                items.addAll(allItems);
                runOnUiThread(() -> {
                    mAdapter = new ItemRecViewAdapter<>(this, items);
                    mAdapter.setClickListener(this);
                    mRecViewItems.setAdapter(mAdapter);
                });
            } catch (Exception e) {
                runOnUiThread(()->{
                    new Warning1().show(SearchActivity.this, "Внимание!",
                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");

                });
            }

        }).start();
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
        Intent intent = new Intent(SearchActivity.this, PassportInfoActivity.class);
        intent.putExtra("PASSPORT_ID", String.valueOf(mAdapter.getItem(position).getId()));
        startActivity(intent);
    }

    /**
     * Создается поле для набора искомого текста
     */
    private void createSearchEditText() {

        //Чтобы исключить появление стандартной клавиатуры
        mEditTextSearch.setShowSoftInputOnFocus (false);
        mEditTextSearch.setCursorVisible(true);
        mEditTextSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mEditTextSearch.setTextIsSelectable(true);
        //Исключить фокус при создании активити
        mEditTextSearch.clearFocus();

        createTextWatcher(mEditTextSearch);

        //При нажатии на поле ввода клавиатура появляется, при потере фокуса
        //при нажатии на список или рестарте активити - клавиатура исчезает
        mEditTextSearch.setOnFocusChangeListener((view, b) -> {
            if(b){
                keyboardContainer.setVisibility(View.VISIBLE);
            } else {
                keyboardContainer.setVisibility(View.GONE);
            }

        });

    }

    /**
     * Создается обработчик изменений текста, происходящих в поле ПОИСК
     * @param mEditTextSearch поле набора поиска
     */
    private void createTextWatcher(EditText mEditTextSearch) {
        mEditTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                new Thread(() -> {
                    if (text == null || text.equals(""))
                        foundItems = allItems;
                    else
                        foundItems = getFoundItems(text);
                    runOnUiThread(() -> {
                        if (foundItems != null)
                            mAdapter.changeListOfItems(foundItems);
                    });

                }).start();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });
    }

    /**
     * Создаем меню для окна с поиском
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    /**
     * Обработка выбора меню
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(SearchActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_update:
                mEditTextSearch.setText("");
                Intent updateView = new Intent(SearchActivity.this, DataLoadingActivity.class);
                startActivity(updateView);
                return true;

            case R.id.action_changeUser:
                mEditTextSearch.setText("");
                Intent loginView = new Intent(SearchActivity.this, LoginActivity.class);
                startActivity(loginView);
                return true;

            case R.id.action_exit:
                exitApplication();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(keyboardContainer.getVisibility() == View.VISIBLE)
            keyboardContainer.setVisibility(View.GONE);
        else
        new AlertDialog.Builder(this)
                .setTitle("Выход тут!")
                .setMessage("Хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        SearchActivity.super.onBackPressed();
                        exitApplication();
                    }
                }).create().show();

    }


    /**
     * Здесь происходит высев подходящих под ПОИСК элементов
     * @param searchText набранный в ПОИСКе текст
     * @return List<P> список подходящих элементов
     */
    private List<Passport> getFoundItems(String searchText){
        List<Passport> foundItems = new ArrayList<>();
        for(Passport item : allItems){
            if(item.toUsefulString().toLowerCase().contains(searchText.toLowerCase()))
                foundItems.add(item);
        }
        return foundItems;
    }



}
