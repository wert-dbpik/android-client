package ru.wert.tubus_mobile.tobusToolbar;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import ru.wert.tubus_mobile.R;

public class TubusToolbar extends Toolbar {

    private TextView tvAppName;
    private TextView tvAlarm;
    private Handler handler;
    private Animation blinkAnimation;
    private boolean isBlinking = false;

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
        tvAlarm = findViewById(R.id.tvAlarm);

        // Настраиваем внешний вид
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // Инициализируем Handler для анимации
        handler = new Handler(Looper.getMainLooper());

        // Создаем анимацию мигания
        blinkAnimation = new AlphaAnimation(0.2f, 1.0f);
        blinkAnimation.setDuration(800);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(Animation.INFINITE);
    }

    // Методы для работы с статусом сервера
    public void showServerStatus(boolean isConnected) {
        if (tvAlarm != null) {
            tvAlarm.setText("СЕРВЕР");
            if (isConnected) {
                // Зеленый цвет при подключении
                tvAlarm.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
            } else {
                // Красный цвет при отключении
                tvAlarm.setTextColor(ContextCompat.getColor(getContext(), R.color.colorMyRed));
            }
            tvAlarm.setVisibility(View.VISIBLE);

            if (!isConnected) {
                startBlinking(); // Мигаем только при отсутствии связи
            } else {
                stopBlinking(); // Прекращаем мигание при подключении
            }
        }
    }

    public void hideServerStatus() {
        if (tvAlarm != null) {
            tvAlarm.setVisibility(View.GONE);
            stopBlinking();
        }
    }

    // Методы для работы с названием приложения
    public void setAppName(String appName) {
        if (tvAppName != null) {
            tvAppName.setText(appName);
        }
    }

    // Методы для работы с тревожным сообщением
    public void showAlarm(String message) {
        if (tvAlarm != null) {
            tvAlarm.setText(message);
            tvAlarm.setVisibility(View.VISIBLE);
            startBlinking();
        }
    }

    public void hideAlarm() {
        if (tvAlarm != null) {
            tvAlarm.setVisibility(View.GONE);
            stopBlinking();
        }
    }

    private void startBlinking() {
        if (!isBlinking) {
            isBlinking = true;
            handler.post(() -> {
                if (tvAlarm != null) {
                    tvAlarm.startAnimation(blinkAnimation);
                }
            });
        }
    }

    private void stopBlinking() {
        if (isBlinking) {
            isBlinking = false;
            handler.post(() -> {
                if (tvAlarm != null) {
                    tvAlarm.clearAnimation();
                }
            });
        }
    }

    // Дополнительные методы
    public void setAlarmColor(int color) {
        if (tvAlarm != null) {
            tvAlarm.setTextColor(color);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBlinking();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
