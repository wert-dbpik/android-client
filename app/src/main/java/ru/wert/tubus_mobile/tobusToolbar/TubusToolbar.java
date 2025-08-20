package ru.wert.tubus_mobile.tobusToolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import ru.wert.tubus_mobile.R;

public class TubusToolbar extends Toolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvAppName;

    public TubusToolbar(Context context) {
        super(context);
        init(context);
    }

    public TubusToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TubusToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Надуваем кастомный layout
        LayoutInflater.from(context).inflate(R.layout.tubus_toolbar, this, true);

        // Инициализируем элементы
        tvAppName = findViewById(R.id.tvAppName);
        tvTitle = findViewById(R.id.tvToolbarTitle);
        tvSubtitle = findViewById(R.id.tvToolbarSubtitle);

        // Настраиваем внешний вид
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // Устанавливаем название приложения
        setAppName("Tubus Mobile");
    }

    // Методы для работы с названием приложения
    public void setAppName(String appName) {
        if (tvAppName != null) {
            tvAppName.setText(appName);
        }
    }

    public void setAppName(int resId) {
        if (tvAppName != null) {
            tvAppName.setText(resId);
        }
    }

    // Методы для работы с заголовком
    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(int resId) {
        if (tvTitle != null) {
            tvTitle.setText(resId);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    public void hideTitle() {
        if (tvTitle != null) {
            tvTitle.setVisibility(View.GONE);
        }
    }

    // Методы для работы с подзаголовком
    public void setSubtitle(String subtitle) {
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        }
    }

    public void setSubtitle(int resId) {
        if (tvSubtitle != null) {
            tvSubtitle.setText(resId);
            tvSubtitle.setVisibility(View.VISIBLE);
        }
    }

    public void hideSubtitle() {
        if (tvSubtitle != null) {
            tvSubtitle.setVisibility(View.GONE);
        }
    }

    // Дополнительные методы
    public void setAppNameColor(int color) {
        if (tvAppName != null) {
            tvAppName.setTextColor(color);
        }
    }

    public void setTitleColor(int color) {
        if (tvTitle != null) {
            tvTitle.setTextColor(color);
        }
    }

    public void setSubtitleColor(int color) {
        if (tvSubtitle != null) {
            tvSubtitle.setTextColor(color);
        }
    }
}
