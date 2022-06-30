package ru.wert.bazapik_mobile.keyboards;

import static ru.wert.bazapik_mobile.organizer.OrganizerActivity.RU_KEYBOARD;
import static ru.wert.bazapik_mobile.organizer.OrganizerActivity.NUM_KEYBOARD;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import ru.wert.bazapik_mobile.R;

public class EngKeyboard extends Fragment implements MyKeyboard{


    @Setter private EditText editTextSearch;//Связь с EditText
    @Setter private KeyboardSwitcher keyboardSwitcher;

    private boolean shiftOn; //false - строчные, true - заглавные

    private List<Button> letterButtons; //Буквенные кнопки
    private Map<Button, String> values; //Пары кнопка-значение

    final private List<String> capitalLetters = Arrays.asList("A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    final private List<String> smallLetters = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

    private Button btnSpace, btnShift, btnClear, btnBackspace, btnLanguage, btn123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fr_eng_keyboard, container, false);

        Button btnA = view.findViewById(R.id.mBtnEngA);
        Button btnB = view.findViewById(R.id.mBtnEngB);
        Button btnC = view.findViewById(R.id.mBtnEngC);
        Button btnD = view.findViewById(R.id.mBtnEngD);
        Button btnE = view.findViewById(R.id.mBtnEngE);
        Button btnF = view.findViewById(R.id.mBtnEngF);
        Button btnG = view.findViewById(R.id.mBtnEngG);
        Button btnH = view.findViewById(R.id.mBtnEngH);
        Button btnI = view.findViewById(R.id.mBtnEngI);
        Button btnJ = view.findViewById(R.id.mBtnEngJ);
        Button btnK = view.findViewById(R.id.mBtnEngK);
        Button btnL = view.findViewById(R.id.mBtnEngL);
        Button btnM = view.findViewById(R.id.mBtnEngM);
        Button btnN = view.findViewById(R.id.mBtnEngN);
        Button btnO = view.findViewById(R.id.mBtnEngO);
        Button btnP = view.findViewById(R.id.mBtnEngP);
        Button btnQ = view.findViewById(R.id.mBtnEngQ);
        Button btnR = view.findViewById(R.id.mBtnEngR);
        Button btnS = view.findViewById(R.id.mBtnEngS);
        Button btnT = view.findViewById(R.id.mBtnEngT);
        Button btnU = view.findViewById(R.id.mBtnEngU);
        Button btnV = view.findViewById(R.id.mBtnEngV);
        Button btnW = view.findViewById(R.id.mBtnEngW);
        Button btnX = view.findViewById(R.id.mBtnEngX);
        Button btnY = view.findViewById(R.id.mBtnEngY);
        Button btnZ = view.findViewById(R.id.mBtnEngZ);

        btnShift = view.findViewById(R.id.mBtnEngShift);
        btnSpace = view.findViewById(R.id.mBtnEngSpace);
        btnClear = view.findViewById(R.id.mBtnEngClear);
        btnBackspace = view.findViewById(R.id.mBtnEngBackspace);
        btnLanguage = view.findViewById(R.id.mBtnEngLanguage);
        btn123 = view.findViewById(R.id.mBtnEng123);

        letterButtons = Arrays.asList(btnA, btnB, btnC, btnD, btnE, btnF, btnG, btnH, btnI, btnJ, btnK, btnL,
                btnM, btnN, btnO, btnP, btnQ, btnR, btnS, btnT, btnU, btnV, btnW, btnX, btnY, btnZ);

        init(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(editTextSearch.isFocused()) {
            //Исправляем косяк с исчеающим фрагментом
            FragmentActivity activity = getActivity();
            FragmentContainerView keyboardContainer = activity.findViewById(R.id.keyboard_container);
            keyboardContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Метод устанавливает заглавные символы
     */
    private void setCapitalLetters(){

        for(int i = 0; i < letterButtons.size(); i++){
            letterButtons.get(i).setText(capitalLetters.get(i));
            values.replace(letterButtons.get(i), capitalLetters.get(i));
        }

    }

    /**
     * Метод устанавливает строчные символы
     */
    private void setSmallLetters(){

        for(int i = 0; i < letterButtons.size(); i++){
            letterButtons.get(i).setText(smallLetters.get(i));
            values.replace(letterButtons.get(i), smallLetters.get(i));
        }

    }

    /**
     * В методе создается HashMap, в котором добавляются пары кнопка - отображаемый символ
     * (он же будет выводиться на экран при нажатии на кнопку)
     * Отдельно создаются специальные служебные кнопки
     */
    private void init(View view)  {

        values = new HashMap<>();

        //A ... Z
        for(int i = 0; i < letterButtons.size(); i++){
            values.put(letterButtons.get(i), smallLetters.get(i));
        }

        //ПРОБЕЛ
        values.put(btnSpace, " ");

        //A ... Z и ПРОБЕЛ
        for(Button b: values.keySet()){
            b.setOnClickListener(v -> {
                StringBuilder text = new StringBuilder(String.valueOf(editTextSearch.getText()));
                int pos = editTextSearch.getSelectionStart();
                editTextSearch.setText(text.insert(pos, values.get(b)));
                editTextSearch.setSelection(pos+1);
            });
        }


        //BACKSPACE
        btnBackspace.setOnClickListener(v->{
            StringBuilder text = new StringBuilder(String.valueOf(editTextSearch.getText()));
            if(editTextSearch.getSelectionStart() !=0) {
                int pos = editTextSearch.getSelectionStart() - 1;
                editTextSearch.setText(text.deleteCharAt(pos));
                editTextSearch.setSelection(pos);
            }
        });

        //SHIFT с подчерком
        btnShift.setOnClickListener(v->{
            shiftOn = !shiftOn;
            if(!shiftOn) {
                btnShift.setText("\u2302");
                setSmallLetters();
            }
            else {
                String s = "\u2302";
                SpannableString ss=new SpannableString(s);
                ss.setSpan(new UnderlineSpan(), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                btnShift.setText(ss);
                setCapitalLetters();
            }

        });

        //CLEAR ALL
        btnClear.setOnClickListener(v->{
            editTextSearch.setText("");
        });

        //RU - ENG
        btnLanguage.setOnClickListener(v->{
            keyboardSwitcher.switchKeyboardTo(RU_KEYBOARD);
        });

        //1 2 3
        btn123.setOnClickListener(v->{
            keyboardSwitcher.switchKeyboardTo(NUM_KEYBOARD);
        });


    }

}