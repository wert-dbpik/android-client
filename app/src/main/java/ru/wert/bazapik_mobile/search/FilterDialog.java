package ru.wert.bazapik_mobile.search;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;

public class FilterDialog extends Dialog{
    private Activity activity;

    public FilterDialog(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filter);

        CheckBox chbShowLegal = findViewById(R.id.chbShowLegal);
        chbShowLegal.setChecked(ThisApplication.showValid);

        CheckBox chbShowChanged = findViewById(R.id.chbShowChanged);
        chbShowChanged.setChecked(ThisApplication.showChanged);

        CheckBox chbShowAnnulled = findViewById(R.id.chbShowAnnulled);
        chbShowAnnulled.setChecked(ThisApplication.showAnnulled);

        setOnDismissListener((e)->{
            ThisApplication.showValid = chbShowLegal.isChecked();
            ThisApplication.showChanged = chbShowChanged.isChecked();
            ThisApplication.showAnnulled = chbShowAnnulled.isChecked();
        });

        Button btnOK = findViewById(R.id.btnFilterOK);
        btnOK.setOnClickListener(e->{
            dismiss();
        });

    }
}
