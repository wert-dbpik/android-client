package ru.wert.bazapik_mobile.warnings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.wert.bazapik_mobile.R;

public class Error extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);


        TextView tvError = findViewById(R.id.errorText);
        Bundle arguments = getIntent().getExtras();
        String error = arguments.get("ERROR").toString();
        tvError.setText(error);

        Button btnOK = findViewById(R.id.btnOK);
        btnOK.setOnClickListener(v -> {
            this.finish();
        });
    }
}
