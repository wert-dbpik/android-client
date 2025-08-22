package ru.wert.tubus_mobile.organizer;

import static ru.wert.tubus_mobile.ThisApplication.APPLICATION_VERSION;
import static ru.wert.tubus_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.tubus_mobile.ThisApplication.APP_VERSION_NOTIFICATION_SHOWN;
import static ru.wert.tubus_mobile.ThisApplication.LIST_OF_ALL_PASSPORTS;
import static ru.wert.tubus_mobile.constants.Consts.USE_APP_KEYBOARD;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import ru.wert.tubus_mobile.ChangePassActivity;
import ru.wert.tubus_mobile.ConnectionToServerActivity;
import ru.wert.tubus_mobile.LoginActivity;
import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.data.models.VersionAndroid;
import ru.wert.tubus_mobile.data.servicesREST.VersionAndroidService;
import ru.wert.tubus_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.tubus_mobile.keyboards.EngKeyboard;
import ru.wert.tubus_mobile.keyboards.KeyboardSwitcher;
import ru.wert.tubus_mobile.keyboards.MyKeyboard;
import ru.wert.tubus_mobile.keyboards.NumKeyboard;
import ru.wert.tubus_mobile.keyboards.RuKeyboard;
import ru.wert.tubus_mobile.main.BaseActivity;
import ru.wert.tubus_mobile.organizer.folders.FoldersFragment;
import ru.wert.tubus_mobile.organizer.folders.FoldersRecViewAdapter;
import ru.wert.tubus_mobile.organizer.history.HistoryManager;
import ru.wert.tubus_mobile.organizer.passports.PassportsFragment;
import ru.wert.tubus_mobile.organizer.passports.PassportsRecViewAdapter;
import ru.wert.tubus_mobile.settings.SettingsActivity;
import ru.wert.tubus_mobile.tobusToolbar.ToolbarHelper;
import ru.wert.tubus_mobile.tobusToolbar.TubusToolbar;
import ru.wert.tubus_mobile.warnings.AppWarnings;

public class OrganizerActivity extends BaseActivity implements KeyboardSwitcher,
        OrgActivityAndFoldersFragmentInteraction, OrgActivityAndPassportsFragmentInteraction {

    @Getter private FragmentManager fm;
    @Getter private PassportsFragment currentPassportsFragment;
    @Getter@Setter private FoldersFragment currentFoldersFragment;
    @Getter@Setter private FragmentTag currentTypeFragment = FragmentTag.FOLDERS_TAG;

    private final String FRAGMENT_TAG = "fragment_tag";
    private static Bundle bundleRecyclerViewState;

    private FragmentContainerView keyboardContainer;
    @Getter private EditText editTextSearch;
    private final List<Fragment> keyboards = new ArrayList<>();
    public static final int NUM_KEYBOARD = 0;
    public static final int RU_KEYBOARD = 1;
    public static final int ENG_KEYBOARD = 2;

    @Getter private Button btnFoldersTab, btnPassportsTab;
    @Getter@Setter private Folder selectedFolder;

    @Getter@Setter private String foldersTextSearch = "";
    @Getter@Setter private String passportsTextSearch = "";
//    private AsyncTask<String, String, Boolean> downloadTask;

    private ImageButton btnHistory;
    private HistoryManager historyManager;
    private PopupMenu historyPopup;

    // Кастомный Toolbar
    private TubusToolbar tubusToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Инициализация кастомного Toolbar
        tubusToolbar = findViewById(R.id.tubusToolbar);
        ToolbarHelper.setupToolbar(this, tubusToolbar);

        // Инициализация HistoryManager
        historyManager = new HistoryManager(this);

        // Находим кнопку истории
        btnHistory = findViewById(R.id.btnHistory);

        // Создаем PopupMenu для отображения истории
        historyPopup = new PopupMenu(this, btnHistory);
        historyPopup.getMenuInflater().inflate(R.menu.empty_menu, historyPopup.getMenu());

        // Обработчик нажатия на кнопку истории
        btnHistory.setOnClickListener(v -> {
            showHistoryPopup();
        });

        fm = getSupportFragmentManager();
        currentFoldersFragment = new FoldersFragment();
        currentPassportsFragment = new PassportsFragment();

        keyboardContainer = findViewById(R.id.keyboard_container);
        editTextSearch = findViewById(R.id.edit_text_search);

        btnFoldersTab = findViewById(R.id.btnFoldersTab);
        btnFoldersTab.setOnClickListener(v->{
            openFoldersFragment();
        });
        btnPassportsTab = findViewById(R.id.btnPassportsTab);
        btnPassportsTab.setOnClickListener(v->{
            openPassportFragment();
        });
        btnPassportsTab.setOnLongClickListener(v -> {
            // Сбрасываем выбранную папку
            setSelectedFolder(null);

            PassportsRecViewAdapter adapter = currentPassportsFragment.getAdapter();
            if(adapter == null) {
                openPassportFragment();
            } else {
                adapter.changeListOfItems(new ArrayList<>(LIST_OF_ALL_PASSPORTS));
                currentPassportsFragment.setCurrentData(new ArrayList<>(currentPassportsFragment.getAdapter().getData()));
            }
            return true; // Возвращаем true, чтобы показать, что событие обработано
        });

        // Слушатель на изменение активного фрагмента меняет содержимое строки поиска
        fm.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(FragmentManager manager, Fragment fragment) {
                super.onFragmentResumed(manager, fragment);
                if (fragment instanceof FoldersFragment)
                    editTextSearch.setText(foldersTextSearch);
                else if (fragment instanceof PassportsFragment)
                    editTextSearch.setText(passportsTextSearch);
            }
        }, true);


        createKeyboards();
        createSearchEditText();
        checkUpNewVersion();

    }



    @Override
    public void onPause() {
        super.onPause();
        bundleRecyclerViewState = new Bundle();
        bundleRecyclerViewState.putString(FRAGMENT_TAG, String.valueOf(currentTypeFragment));
    }

    /**
     * При рестарте боремся с появлением стандартной клавиатурой
     */
    @Override
    public void onResume() {
        super.onResume();
        editTextSearch.clearFocus();

        if(USE_APP_KEYBOARD) switchOffStandardKeyboard();
        else switchOnStandardKeyboard();

        if (bundleRecyclerViewState != null) {
            currentTypeFragment = FragmentTag.valueOf(bundleRecyclerViewState.getString(FRAGMENT_TAG));
            openCurrentFragment();
        }
    }

    private void openCurrentFragment(){
        if(currentTypeFragment.equals(FragmentTag.FOLDERS_TAG)) {
            openFoldersFragment();
        }
        else if(currentTypeFragment.equals(FragmentTag.PASSPORT_TAG)) {
            openPassportFragment();
        }
    }


    public void fragmentChanged(OrganizerFragment newFragment){
        btnPassportsTab.setTextColor(getResources().getColor(R.color.cardview_dark_background, null));
        btnPassportsTab.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));

        btnFoldersTab.setTextColor(getResources().getColor(R.color.cardview_dark_background, null));
        btnFoldersTab.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        //============================================================================================================
        if(newFragment instanceof PassportsFragment){
            btnPassportsTab.setTextColor(getResources().getColor(R.color.colorAccent, null));
            btnPassportsTab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
        }
        else if (newFragment instanceof FoldersFragment){
            btnFoldersTab.setTextColor(getResources().getColor(R.color.colorAccent, null));
            btnFoldersTab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
        }
    }

    private void checkUpNewVersion(){
        new Thread(()->{
            List<VersionAndroid> alVersions = VersionAndroidService.getInstance().findAll();
            if(alVersions != null && !alVersions.isEmpty()) {

                APPLICATION_VERSION_AVAILABLE = alVersions.get(alVersions.size() - 1).getName();
                if (APPLICATION_VERSION_AVAILABLE.compareTo(ThisApplication.APPLICATION_VERSION) > 0 &&
                        !APP_VERSION_NOTIFICATION_SHOWN) {

                    APP_VERSION_NOTIFICATION_SHOWN = true;
                    runOnUiThread(()-> AppWarnings.showAlert_NewAppVersionAvailable(OrganizerActivity.this));
                }
            } else {
                runOnUiThread(()-> AppWarnings.showAlert_NoAppVersionsAvailable(OrganizerActivity.this));
                APPLICATION_VERSION_AVAILABLE = APPLICATION_VERSION;
            }
        }).start();
    }

    /**
     * Создается поле для набора искомого текста
     */
    private void createSearchEditText() {
        editTextSearch.setCursorVisible(true);
        editTextSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editTextSearch.setTextIsSelectable(true);

        createTextWatcher(editTextSearch);

    }
    
    private void switchOffStandardKeyboard(){
        editTextSearch.setShowSoftInputOnFocus(false);

        //При нажатии на поле ввода клавиатура появляется, при потере фокуса
        //при нажатии на список или рестарте активити - клавиатура исчезает
        editTextSearch.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                keyboardContainer.setVisibility(View.VISIBLE);
            } else {
                keyboardContainer.setVisibility(View.GONE);
            }
        });
    }

    private void switchOnStandardKeyboard(){
        editTextSearch.setShowSoftInputOnFocus(true);
        editTextSearch.setOnFocusChangeListener(null);
    }


    @Override
    public void onBackPressed() {
        OrganizerFragment<Item> fr = (OrganizerFragment) fm.findFragmentById(R.id.organizer_fragment_container);

        if(USE_APP_KEYBOARD && keyboardContainer.getVisibility() == View.VISIBLE) {
            editTextSearch.clearFocus();
            if(USE_APP_KEYBOARD) keyboardContainer.setVisibility(View.GONE);
            else {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
            }
        } else if(fr instanceof FoldersFragment) {
            if(currentFoldersFragment.getUpperProductGroupId().equals(1L))
                showAlertDialogAndExit();
            else{
                Item i = ((FoldersRecViewAdapter)currentFoldersFragment.getAdapter()).getItem(0);
                if(i instanceof Folder){ //Если папка, значит имеем список папок
                    showAlertDialogAndExit();
                } else {
                    FragmentTransaction ft = fm.beginTransaction();
                    // Убираем анимацию для возврата
                    ft.setCustomAnimations(0, 0);
                    ft.replace(R.id.organizer_fragment_container, currentFoldersFragment);
                    ft.commit();
                    currentFoldersFragment.onItemClick(currentFoldersFragment.getView(), 0);
                }
            }
        } else if(fr instanceof PassportsFragment){
            FragmentTransaction ft = fm.beginTransaction();
            // Анимация только справа налево при переходе к папкам
            ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
            ft.replace(R.id.organizer_fragment_container, currentFoldersFragment);
            ft.commit();
        }
    }


    private void showAlertDialogAndExit(){
        new AlertDialog.Builder(this)
                .setMessage("Хотите закрыть приложение?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        exitApplication();
                    }
                }).create().show();
    }


    /**
     * Создается обработчик изменений текста, происходящих в поле ПОИСК
     * @param mEditTextSearch поле набора поиска
     */
    private void createTextWatcher(EditText mEditTextSearch) {
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEditTextSearch.hasFocus() || isFormatting) return;

                String text = s.toString();

                if (text.matches("\\d{7}")) {
                    isFormatting = true;
                    String formattedText = text.substring(0, 6) + "." + text.substring(6);

                    // Устанавливаем новый текст
                    mEditTextSearch.setText(formattedText);

                    // Задержка для корректного позиционирования курсора
                    mEditTextSearch.post(() -> {
                        mEditTextSearch.setSelection(formattedText.length()); // Курсор в конец
                        isFormatting = false;
                    });

                    searchByText(formattedText);
                    return;
                }

                searchByText(text);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    // Показываем popup с историей
    private void showHistoryPopup() {
        Menu menu = historyPopup.getMenu();
        menu.clear();

        List<String> history = historyManager.getHistory();
        if (history.isEmpty()) {
            menu.add("История пуста").setEnabled(false);
        } else {
            for (int i = 0; i < history.size(); i++) {
                menu.add(0, i, 0, history.get(i));
            }
        }

        historyPopup.setOnMenuItemClickListener(item -> {
            String selectedDrawing = history.get(item.getItemId());

            // Извлекаем номер по маскам (децимальный номер или Э+5 цифр)
            String searchNumber = extractDrawingNumber(selectedDrawing);

            // Устанавливаем в поле поиска
            editTextSearch.setText(searchNumber);

            editTextSearch.requestFocus();

            // Перемещаем курсор в конец текста
            editTextSearch.setSelection(searchNumber.length());

            // Ищем по извлеченному номеру
            searchByText(searchNumber);
            return true;
        });

        historyPopup.show();
    }

    private String extractDrawingNumber(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Паттерн для децимального номера (например: ПИК.301222.255)
        Pattern decimalPattern = Pattern.compile("[А-ЯA-Z]+\\.[0-9]+\\.[0-9]+");
        Matcher decimalMatcher = decimalPattern.matcher(text);

        if (decimalMatcher.find()) {
            return decimalMatcher.group(); // Возвращаем найденный децимальный номер
        }

        // Паттерн для номера типа Э13457 (буква Э и 5 цифр)
        Pattern ePattern = Pattern.compile("Э[0-9]{5}");
        Matcher eMatcher = ePattern.matcher(text);

        if (eMatcher.find()) {
            return eMatcher.group(); // Возвращаем найденный номер типа Э13457
        }

        // Если ни одна маска не подошла, возвращаем исходный текст
        return text;
    }

    private void searchByText(String text) {

        new Thread(() -> {
            List<Item> items = null;
            OrganizerFragment<Item> fr = (OrganizerFragment) fm.findFragmentById(R.id.organizer_fragment_container);

            if(fr instanceof FoldersFragment){
                foldersTextSearch = text;
                if(text.isEmpty())
                    items = ((FoldersFragment) fr).currentListWithGlobalOff(null);
                else
                    items = fr.findProperItems(text);
            } else if(fr instanceof PassportsFragment){
                passportsTextSearch = text;
                if(text.isEmpty())
                    items = currentPassportsFragment.getCurrentData();
                else
                    items = fr.findProperItems(text);
            }

            List<Item> finalItems = items;
            runOnUiThread(() -> {
                if (fr != null && fr.getAdapter() != null && finalItems != null) {
                    fr.getAdapter().changeListOfItems(finalItems);
                }
            });
        }).start();
    }

    /**
     * Создаем нашу нестандартную клавиатуру,
     * состояющую из ЦИФРОВОЙ и ТЕКСТОВОЙ клавиатур
     */
    private void createKeyboards() {

        NumKeyboard numberKeyboard = new NumKeyboard();
        numberKeyboard.setKeyboardSwitcher(this);
        numberKeyboard.setEditTextSearch(editTextSearch);
        keyboards.add(NUM_KEYBOARD, numberKeyboard);

        RuKeyboard ruKeyboard = new RuKeyboard();
        ruKeyboard.setKeyboardSwitcher(this);
        ruKeyboard.setEditTextSearch(editTextSearch);
        keyboards.add(RU_KEYBOARD, ruKeyboard);

        EngKeyboard engKeyboard = new EngKeyboard();
        engKeyboard.setKeyboardSwitcher(this);
        engKeyboard.setEditTextSearch(editTextSearch);
        keyboards.add(ENG_KEYBOARD, engKeyboard);

        switchKeyboardTo(NUM_KEYBOARD);

    }

    @Override
    public void switchKeyboardTo(int keyboard) {
        MyKeyboard myKeyboard = (MyKeyboard) keyboards.get(keyboard);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.keyboard_container, (Fragment) myKeyboard, "keyboard_tag");
        ft.commitAllowingStateLoss();
    }


    public void openPassportFragment() {
        currentPassportsFragment = new PassportsFragment();
        currentPassportsFragment.setHistoryManager(historyManager);

        FragmentTransaction ft = fm.beginTransaction();

        // Применяем анимацию только если текущий фрагмент - FoldersFragment
        if (fm.findFragmentById(R.id.organizer_fragment_container) instanceof FoldersFragment) {
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        } else {
            // Без анимации для других случаев
            ft.setCustomAnimations(0, 0);
        }

        ft.replace(R.id.organizer_fragment_container, currentPassportsFragment);
        ft.commitAllowingStateLoss();
    }

    public void openFoldersFragment() {

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.organizer_fragment_container, currentFoldersFragment);
//        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();

        //Чтобы выделение строки не пропадало на первом фрагменте не пропадало
        if (currentFoldersFragment.getUpperProductGroupId() != null &&
                currentFoldersFragment.getUpperProductGroupId().equals(1L))
            getFm().getFragments().clear();
    }

    //=======================  M E N U  ================================

    /**
     * Создаем меню для окна с поиском
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
                Intent settingsIntent = new Intent(OrganizerActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_update:
                editTextSearch.setText("");
                // Запускаем обновление с принудительной перезагрузкой с сервера
                Intent updateView = new Intent(OrganizerActivity.this, DataLoadingActivity.class);
                updateView.putExtra("FORCE_REFRESH", true); // Флаг принудительного обновления
                startActivity(updateView);
                return true;

//            case R.id.action_showChat:
//                editTextSearch.setText("");
//                Intent chatView = new Intent(OrganizerActivity.this, ChatActivity.class);
//                startActivity(chatView);
//                return true;

            case R.id.action_changeUser:
                editTextSearch.setText("");
                Intent loginView = new Intent(OrganizerActivity.this, LoginActivity.class);
                startActivity(loginView);
                return true;

            case R.id.action_changeServer:
                editTextSearch.setText("");
                Intent сonnectionToServer = new Intent(OrganizerActivity.this, ConnectionToServerActivity.class);
                сonnectionToServer.putExtra("RECONNECT", true); // Флаг принудительного обновления
                startActivity(сonnectionToServer);
                return true;

            case R.id.action_changePass:
                editTextSearch.setText("");
                Intent changePassView = new Intent(OrganizerActivity.this, ChangePassActivity.class);
                startActivity(changePassView);
                return true;

            case R.id.action_showFilterDialog:
                FilterDialog filterDialog = new FilterDialog(OrganizerActivity.this);
                filterDialog.show();
                return true;

            case R.id.action_clearHistory:
                historyManager.clearHistory();
                return true;

            case R.id.action_exit:
                exitApplication();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

