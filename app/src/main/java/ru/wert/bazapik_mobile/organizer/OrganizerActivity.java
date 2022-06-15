package ru.wert.bazapik_mobile.organizer;

import static ru.wert.bazapik_mobile.organizer.EFragments.*;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.keyboards.EngKeyboard;
import ru.wert.bazapik_mobile.keyboards.KeyboardSwitcher;
import ru.wert.bazapik_mobile.keyboards.MyKeyboard;
import ru.wert.bazapik_mobile.keyboards.NumKeyboard;
import ru.wert.bazapik_mobile.keyboards.RuKeyboard;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.utils.Direction;

public class OrganizerActivity extends BaseActivity implements KeyboardSwitcher {

    @Getter private FragmentManager fm;
    @Getter private PassportsFragment passportsFragment;
    @Getter private FoldersFragment foldersFragment;
    private List<EFragments> fragments = Arrays.asList(FRAG_FOLDERS, FRAG_PASSPORTS);
    private Direction direction = Direction.NEXT;
    private int currentFragment = 0;

    private FragmentContainerView keyboardContainer;
    @Getter private EditText editTextSearch;
    private final List<Fragment> keyboards = new ArrayList<>();
    public static final int NUM_KEYBOARD = 0;
    public static final int RU_KEYBOARD = 1;
    public static final int ENG_KEYBOARD = 2;

    @Getter@Setter
    private Folder selectedFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        fm = getSupportFragmentManager();
        passportsFragment = new PassportsFragment();
        foldersFragment = new FoldersFragment();

        keyboardContainer = findViewById(R.id.keyboard_container);
        editTextSearch = findViewById(R.id.edit_text_search);

        createKeyboards();
        createSearchEditText();

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
                    if (text == null || text.equals("")){
                        if(fr instanceof FoldersFragment){
                            List<Item> items = ((FoldersFragment)fr).currentListWithGlobalOff();
                             ((FoldersFragment)fr).fillRecViewWithItems(items);
                             return;
                        } else
                        fr.setFoundItems(fr.getAllItems());
                }else
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

    @Override
    protected void onStart() {
        super.onStart();

        currentFragment = 1;
        openFragment();
    }

    private void openFragment() {
        switch (fragments.get(currentFragment)) {
            case FRAG_FOLDERS:
                openFoldersFragment();
                break;
            case FRAG_PASSPORTS:
                openPassportFragment();
                break;
        }
    }

    public void openPassportFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        if (direction.equals(Direction.NEXT))
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        else
            ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.organizer_fragment_container, (Fragment) passportsFragment, "passports_tag");
        ft.commit();
    }

    private void openFoldersFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        if (direction.equals(Direction.NEXT))
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        else
            ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.organizer_fragment_container, (Fragment) foldersFragment, "folders_tag");
        ft.commit();
    }

    public AppOnSwipeTouchListener createOnSwipeTouchListener() {
        return new AppOnSwipeTouchListener(OrganizerActivity.this) {
            public void onSwipeRight() {   //назад
                if (currentFragment != 0) {
                    currentFragment--;
                    direction = Direction.PREV;
                    openFragment();
                }
            }

            public void onSwipeLeft() {  //вперед
                if (currentFragment < fragments.size() - 1) {
                    currentFragment++;
                    direction = Direction.NEXT;
                    openFragment();
                }
            }
        };
    }
}

enum EFragments {
    FRAG_FOLDERS,
    FRAG_PASSPORTS;
}