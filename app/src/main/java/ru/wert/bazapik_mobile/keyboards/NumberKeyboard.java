package ru.wert.bazapik_mobile.keyboards;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import ru.wert.bazapik_mobile.R;

public class NumberKeyboard extends Fragment {

    private final String TAG = getClass().getSimpleName();
    static final String DEFAULT_PREFIX = "ПИК"; //использовать дефолтное
    private EditText mEditTextSearch;//Связь с EditText


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fr_number_keyboard, container, false);

        init(view);

        return view;
    }

    /**
     * В методе создается HashMap, в котором добавляются пары кнопка - отображаемый символ
     * (он же будет выводиться на экран при нажатии на кнопку)
     * Отдельно создаются специальные служебные кнопки
     * @param view
     */
    private void init(View view)  {

        Map<Button, String> buttons = new HashMap<>();
        buttons.put(view.findViewById(R.id.mBtn0), "0");
        buttons.put(view.findViewById(R.id.mBtn1), "1");
        buttons.put(view.findViewById(R.id.mBtn2), "2");
        buttons.put(view.findViewById(R.id.mBtn3), "3");
        buttons.put(view.findViewById(R.id.mBtn4), "4");
        buttons.put(view.findViewById(R.id.mBtn5), "5");
        buttons.put(view.findViewById(R.id.mBtn6), "6");
        buttons.put(view.findViewById(R.id.mBtn7), "7");
        buttons.put(view.findViewById(R.id.mBtn8), "8");
        buttons.put(view.findViewById(R.id.mBtn9), "9");
        buttons.put(view.findViewById(R.id.mBtnDash), "-");
        buttons.put(view.findViewById(R.id.mBtnEngDot), ".");

        //Каждой кнопке из HashMap buttons назначается слушатель OnClickListener
        //При нажатии на кнопку выводится символ который соответствует кнопке в HashMap buttons
        for(Button b: buttons.keySet()){
            b.setOnClickListener(v -> {
                StringBuilder text = new StringBuilder(String.valueOf(mEditTextSearch.getText()));
                int pos = mEditTextSearch.getSelectionStart();
                mEditTextSearch.setText(text.insert(pos,buttons.get(b)));
                mEditTextSearch.setSelection(pos+1);
            });
        }

        //Специальная кнопка Backspace <=
        final Button mBtnBackspace = view.findViewById(R.id.mBtnEngBackspace);
        mBtnBackspace.setOnClickListener(v->{
            StringBuilder text = new StringBuilder(String.valueOf(mEditTextSearch.getText()));
            if(mEditTextSearch.getSelectionStart() !=0) {
                int pos = mEditTextSearch.getSelectionStart() - 1;
                mEditTextSearch.setText(text.deleteCharAt(pos));
                mEditTextSearch.setSelection(pos);
            }
        });

        //Специальная кнопка Clear
        final Button mBtnCLear = view.findViewById(R.id.mBtnEngClear);
        mBtnCLear.setOnClickListener(v->{
            mEditTextSearch.setText("");
        });

        //Специальная кнопка PIK - выводт префикс по умолчанию
        final Button mBtnPIK = view.findViewById(R.id.mBtnPIK);
        mBtnPIK.setOnClickListener(v->{
            mEditTextSearch.setText(DEFAULT_PREFIX);
            mEditTextSearch.setSelection(DEFAULT_PREFIX.length());
        });

        final Button mBtnText = view.findViewById(R.id.mBtnText);
        mBtnText.setOnClickListener(v->{

        });
    }

    public void setEditTextSearch(EditText editText){
        this.mEditTextSearch = editText;
    }
}