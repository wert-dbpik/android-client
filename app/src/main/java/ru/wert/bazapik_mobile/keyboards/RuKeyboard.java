package ru.wert.bazapik_mobile.keyboards;

import static ru.wert.bazapik_mobile.organizer.OrganizerActivity.ENG_KEYBOARD;
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
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import ru.wert.bazapik_mobile.R;

public class RuKeyboard extends Fragment implements MyKeyboard{

    @Setter private EditText editTextSearch;//Связь с EditText

    @Setter private KeyboardSwitcher keyboardSwitcher;

    private List<Button> letterButtons; //Буквенные кнопки
    private Map<Button, String> values; //Пары кнопка-значение

    private Button btnSpace, btnShift, btnBackspace, btnLanguage, btn123;
    private ImageButton btnSearchNow;

    final private List<String> capitalLetters = Arrays.asList("А", "Б", "В", "Г", "Д", "Е", "Ж",
            "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч",
            "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я");

    final private List<String> smallLetters = Arrays.asList("а", "б", "в", "г", "д", "е", "ж",
            "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч",
            "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я");


    private boolean shiftOn; //false - строчные, true - заглавные

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fagment_ru_keyboard, container, false);

        Button btnA = view.findViewById(R.id.mBtnRuA);
        Button btnB = view.findViewById(R.id.mBtnRuB);
        Button btnV = view.findViewById(R.id.mBtnRuV);
        Button btnG = view.findViewById(R.id.mBtnRuG);
        Button btnD = view.findViewById(R.id.mBtnRuD);
        Button btnE = view.findViewById(R.id.mBtnRuE);
        Button btnZsh = view.findViewById(R.id.mBtnRuZsh);
        Button btnZ = view.findViewById(R.id.mBtnRuZ);
        Button btnI = view.findViewById(R.id.mBtnRuI);
        Button btnJ = view.findViewById(R.id.mBtnRuJ);
        Button btnK = view.findViewById(R.id.mBtnRuK);
        Button btnL = view.findViewById(R.id.mBtnRuL);
        Button btnM = view.findViewById(R.id.mBtnRuM);
        Button btnN = view.findViewById(R.id.mBtnRuN);
        Button btnO = view.findViewById(R.id.mBtnRuO);
        Button btnP = view.findViewById(R.id.mBtnRuP);
        Button btnR = view.findViewById(R.id.mBtnRuR);
        Button btnS = view.findViewById(R.id.mBtnRuS);
        Button btnT = view.findViewById(R.id.mBtnRuT);
        Button btnU = view.findViewById(R.id.mBtnRuU);
        Button btnF = view.findViewById(R.id.mBtnRuF);
        Button btnH = view.findViewById(R.id.mBtnRuH);
        Button btnTs = view.findViewById(R.id.mBtnRuTs);
        Button btnCh = view.findViewById(R.id.mBtnRuCh);
        Button btnSh = view.findViewById(R.id.mBtnRuSh);
        Button btnTsh = view.findViewById(R.id.mBtnRuTsh);
        Button btnHardSign = view.findViewById(R.id.mBtnRuHardSign);
        Button btnYi = view.findViewById(R.id.mBtnRuYi);
        Button btnSoftSign = view.findViewById(R.id.mBtnRuSoftSign);
        Button btnYe = view.findViewById(R.id.mBtnRuYe);
        Button btnYu = view.findViewById(R.id.mBtnRuYu);
        Button btnYa = view.findViewById(R.id.mBtnRuYa);

        btnShift = view.findViewById(R.id.mBtnRuShift);
        btnSpace = view.findViewById(R.id.mBtnRuSpace);
        btnSearchNow = view.findViewById(R.id.mBtnRuSearchNow);
        btnBackspace = view.findViewById(R.id.mBtnRuBackspace);
        btnLanguage = view.findViewById(R.id.mBtnRuLanguage);
        btn123 = view.findViewById(R.id.mBtnRu123);

        letterButtons = Arrays.asList(btnA, btnB, btnV, btnG, btnD, btnE, btnZsh, btnZ, btnI, btnJ, btnK, btnL,
                btnM, btnN, btnO, btnP, btnR, btnS, btnT, btnU, btnF, btnH, btnTs, btnCh, btnSh,
                btnTsh, btnHardSign, btnYi, btnSoftSign, btnYe, btnYu, btnYa);

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

//        editTextSearch.requestFocus();

        values = new HashMap<>();

        //А ... Я
        for(int i = 0; i < letterButtons.size(); i++){
            values.put(letterButtons.get(i), smallLetters.get(i));
        }

        //ПРОБЕЛ
        values.put(btnSpace, " ");

        //А ... Я и ПРОБЕЛ
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

        btnBackspace.setOnLongClickListener(v->{
            editTextSearch.setText("");
            return true;
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
        btnSearchNow.setOnClickListener(v->{
            String text = editTextSearch.getText().toString();
            editTextSearch.setText(text);
            editTextSearch.setSelection(editTextSearch.length());
        });

        //RU - ENG
        btnLanguage.setOnClickListener(v->{
            keyboardSwitcher.switchKeyboardTo(ENG_KEYBOARD);
        });

        //1 2 3
        btn123.setOnClickListener(v->{
            keyboardSwitcher.switchKeyboardTo(NUM_KEYBOARD);
        });


    }

}