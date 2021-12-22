package ru.wert.bazapik_mobile.search;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.constants.StaticMethods;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.info.PassportInfoActivity;
import ru.wert.bazapik_mobile.keyboards.NumberKeyboard;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.settings.SettingsActivity;
import ru.wert.bazapik_mobile.warnings.Warning1;

import static ru.wert.bazapik_mobile.ThisApplication.FOUND_ITEMS;
import static ru.wert.bazapik_mobile.ThisApplication.SEARCH_TEXT;

/**
 * Окно поиска чертежа.
 * Состоит из
 * 1) текстового поля поиска - mEditTextSearch
 * 2) Списка найденных элементов (Passport) - mRecViewItems
 * 3) Всплывающей клавиатуры - keyboardView
 *
 */
public class SearchActivity extends BaseActivity implements ItemRecViewAdapter.ItemClickListener{
    private static final String TAG = "+++ SearchActivity +++";

    private ItemRecViewAdapter<Passport> mAdapter;
    private RecyclerView mRecViewItems;
    private List<Passport> allItems;
    private List<Passport> foundItems;
    private EditText mEditTextSearch;
    private FragmentContainerView keyboardView;
    private int oldOrientation;

    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            String searchedText = savedInstanceState.getString("searchedText");
            if(searchedText != null) mEditTextSearch.setText(searchedText);

            foundItems.clear();
            foundItems.addAll(FOUND_ITEMS);
        }

        keyboardView = findViewById(R.id.keyboard_fragment);
        mEditTextSearch = findViewById(R.id.edit_text_search);
        mRecViewItems = findViewById(R.id.recycle_view_items);

        createKeyboards();
        createSearchEditText();
        createRecycleViewOfFoundItems();

        if(!SEARCH_TEXT.equals(""))
            mEditTextSearch.setText(SEARCH_TEXT);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("searchedText", mEditTextSearch.getText().toString());
            FOUND_ITEMS = (List<Passport>) foundItems;

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != oldOrientation){
            foundItems.clear();
            foundItems.addAll(FOUND_ITEMS);
        }
    }


    /**
     * При рестарте боремся с поялением стандартной клавиатурой
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        //Позволяет избежать появления стандартной клавиатуры при рестарте активити
        mEditTextSearch.clearFocus();
        mEditTextSearch.setText(SEARCH_TEXT);

    }

    @Override
    protected void onPause() {
        super.onPause();
        SEARCH_TEXT = mEditTextSearch.getText().toString();

    }

    /**
     * Создаем нашу нестандартную клавиатуру,
     * состояющую из ЦИФРОВОЙ и ТЕКСТОВОЙ клавиатур
     */
    private void createKeyboards() {
        NumberKeyboard numberKeyboard = (NumberKeyboard)
                getSupportFragmentManager().findFragmentById(R.id.keyboard_fragment);
        //Связываем поле поиска с клавиатурой
        numberKeyboard.setEditTextSearch(mEditTextSearch);
    }

    /**
     * Создаем список состоящий из найденных элементов
     */
    private void createRecycleViewOfFoundItems() {

        mRecViewItems.setLayoutManager(new LinearLayoutManager(this));

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
                keyboardView.setVisibility(View.VISIBLE);
            } else {
                keyboardView.setVisibility(View.GONE);
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
     * @param item
     * @return
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
                Intent updateView = new Intent(SearchActivity.this, DataLoadingActivity.class);
                startActivity(updateView);
                return true;
            case R.id.action_exit:
                exitApplication();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(keyboardView.getVisibility() == View.VISIBLE)
            keyboardView.setVisibility(View.GONE);
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
            if(item.toUsefulString().contains(searchText))
                foundItems.add(item);
        }
        return foundItems;
    }


}
