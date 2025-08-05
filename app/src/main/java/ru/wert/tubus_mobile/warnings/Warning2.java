package ru.wert.tubus_mobile.warnings;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import ru.wert.tubus_mobile.R;

public class Warning2 extends Dialog{
    private Activity activity;
    private final String attention;
    private final String problem;
    private boolean res;

    public Warning2(@NonNull Activity activity, String attention,  String problem) {
        super(activity);
        this.activity = activity;
        this.attention = attention;
        this.problem = problem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.warning2);

        TextView tvAttention = findViewById(R.id.tvAttention);
        tvAttention.setText(attention);

        TextView tvProblem = findViewById(R.id.tvProblem);
        tvProblem.setText(problem);

        Button btnCancel = findViewById(R.id.btnWarning2Cancel);
        btnCancel.setOnClickListener(e->{
            res = false;
            dismiss();
        });

        Button btnOK = findViewById(R.id.btnWarning2Ok);
        btnOK.setOnClickListener(e->{
            res = true;
            dismiss();
        });

    }
}
