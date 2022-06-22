package ru.wert.bazapik_mobile.organizer;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;
import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;
import static ru.wert.bazapik_mobile.ThisApplication.APP_VERSION_NOTIFICATION_SHOWN;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ru.wert.bazapik_mobile.ChangePassActivity;
import ru.wert.bazapik_mobile.LoginActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.VersionAndroid;
import ru.wert.bazapik_mobile.data.servicesREST.VersionAndroidService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.keyboards.EngKeyboard;
import ru.wert.bazapik_mobile.keyboards.KeyboardSwitcher;
import ru.wert.bazapik_mobile.keyboards.MyKeyboard;
import ru.wert.bazapik_mobile.keyboards.NumKeyboard;
import ru.wert.bazapik_mobile.keyboards.RuKeyboard;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.folders.FoldersFragment;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;
import ru.wert.bazapik_mobile.settings.SettingsActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class OrganizerActivity extends BaseActivity implements KeyboardSwitcher {

    @Getter private FragmentManager fm;
    @Getter private PassportsFragment currentPassportsFragment;
    @Getter@Setter private FoldersFragment currentFoldersFragment;
    @Getter@Setter private FragmentTag currentFragment = FragmentTag.FOLDERS_TAG;

    private final String FRAGMENT_TAG = "fragment_tag";
    private final String POSITION = "position";
    private static Bundle bundleRecyclerViewState;

    private FragmentContainerView keyboardContainer;
    @Getter private EditText editTextSearch;
    private final List<Fragment> keyboards = new ArrayList<>();
    public static final int NUM_KEYBOARD = 0;
    public static final int RU_KEYBOARD = 1;
    public static final int ENG_KEYBOARD = 2;

    @Getter private Button btnFoldersTab, btnPassportsTab;

    @Getter@Setter
    private Folder selectedFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        fm = getSupportFragmentManager();
        currentPassportsFragment = new PassportsFragment();
        currentFoldersFragment = new FoldersFragment();

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
            currentPassportsFragment.getAdapter().changeListOfItems(new ArrayList<>(ALL_PASSPORTS));
            return false;
        });

        createKeyboards();
        createSearchEditText();
        checkUpNewVersion();

    }

    @Override
    public void onPause() {
        super.onPause();
        bundleRecyclerViewState = new Bundle();
        bundleRecyclerViewState.putString(FRAGMENT_TAG, String.valueOf(currentFragment));
    }

    /**
     * При рестарте боремся с появлением стандартной клавиатурой
     */
    @Override
    public void onResume() {
        super.onResume();
        if (bundleRecyclerViewState != null) {
            currentFragment = FragmentTag.valueOf(bundleRecyclerViewState.getString(FRAGMENT_TAG));
            openCurrentFragment();
        }
    }

    private void openCurrentFragment(){
        if(currentFragment.equals(FragmentTag.FOLDERS_TAG)) openFoldersFragment();
        else if(currentFragment.equals(FragmentTag.PASSPORT_TAG)) openPassportFragment();
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
            APPLICATION_VERSION_AVAILABLE = alVersions.get(alVersions.size()-1).getName();
            if(APPLICATION_VERSION_AVAILABLE.compareTo(ThisApplication.APPLICATION_VERSION) > 0 &&
                    !APP_VERSION_NOTIFICATION_SHOWN) {

                APP_VERSION_NOTIFICATION_SHOWN = true;
                runOnUiThread(() -> {
                    new WarningDialog1().show(OrganizerActivity.this,
                            "Внимание!",
                            String.format("Доступна новая версия %s. Чтобы скачать и обновить программу " +
                                    "зайдите в настройки и кликните по мигающей надписи. Далее: " +
                                    "установите программу из скачанного файла apk", APPLICATION_VERSION_AVAILABLE));
                });
            }
        }).start();
    }

    /**
     * Создается поле для набора искомого текста
     */
    private void createSearchEditText() {

        //Чтобы исключить появление стандартной клавиатуры
        editTextSearch.setShowSoftInputOnFocus (false);
        editTextSearch.setCursorVisible(true);
        editTextSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editTextSearch.setTextIsSelectable(true);
        //Исключить фокус при создании активити
        editTextSearch.clearFocus();

        createTextWatcher(editTextSearch);

        //При нажатии на поле ввода клавиатура появляется, при потере фокуса
        //при нажатии на список или рестарте активити - клавиатура исчезает
        editTextSearch.setOnFocusChangeListener((view, b) -> {
            if(b){
                keyboardContainer.setVisibility(View.VISIBLE);
            } else {
                keyboardContainer.setVisibility(View.GONE);
            }

        });

    }

    @Override
    public void onBackPressed() {
        OrganizerFragment<Item> fr = (OrganizerFragment) fm.findFragmentById(R.id.organizer_fragment_container);
        if(keyboardContainer.getVisibility() == View.VISIBLE) {
            keyboardContainer.setVisibility(View.GONE);
        } else if(fr instanceof FoldersFragment) {
            if(currentFoldersFragment.getCurrentProductGroupId().equals(1L))
                showAlertDialogAndExit();
            else{
                currentFoldersFragment.onItemClick(currentFoldersFragment.getView(), 0);
            }
        } else if(fr instanceof PassportsFragment){
                openFoldersFragment();
        }

    }

    private void showAlertDialogAndExit(){
        new AlertDialog.Builder(this)
                .setTitle("Выход тут!")
                .setMessage("Хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        OrganizerActivity.super.onBackPressed();
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

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                new Thread(() -> {
                    OrganizerFragment<Item> fr = (OrganizerFragment) fm.findFragmentById(R.id.organizer_fragment_container);
                    if (text == null || text.equals("")) {
                        if (fr instanceof FoldersFragment) {
                            List<Item> items = ((FoldersFragment) fr).currentListWithGlobalOff(null);
                            ((FoldersFragment) fr).fillRecViewWithItems(items);
                            return;
                        } else
                            fr.setFoundItems(fr.getAllItems());
                    } else
                        fr.setFoundItems(fr.findProperItems(text));
                    runOnUiThread(() -> {
                        if (fr.getFoundItems() != null)
                            fr.getAdapter().changeListOfItems(fr.getFoundItems());
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
        ft.commit();
    }


    public void openPassportFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        ft.replace(R.id.organizer_fragment_container, currentPassportsFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void openFoldersFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.organizer_fragment_container, currentFoldersFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    //=======================  M E N U  ================================

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
                Intent settingsIntent = new Intent(OrganizerActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_update:
                editTextSearch.setText("");
                Intent updateView = new Intent(OrganizerActivity.this, DataLoadingActivity.class);
                startActivity(updateView);
                return true;

            case R.id.action_changeUser:
                editTextSearch.setText("");
                Intent loginView = new Intent(OrganizerActivity.this, LoginActivity.class);
                startActivity(loginView);
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

            case R.id.action_exit:
                exitApplication();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

