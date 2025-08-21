package ru.wert.tubus_mobile.tobusToolbar;

import androidx.appcompat.app.AppCompatActivity;

import ru.wert.tubus_mobile.heartbeat.ConnectionManager;

public class ToolbarHelper {

    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar, String appName) {
        setupToolbar(activity, tubusToolbar);
        updateAppName(tubusToolbar, appName);
    }

    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar) {
        if (tubusToolbar == null) return;

        try {
            // Устанавливаем кастомный Toolbar как ActionBar
            activity.setSupportActionBar(tubusToolbar);

            // Скрываем стандартный заголовок
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Регистрируем тулбар в ConnectionManager для отображения статуса
            ConnectionManager.getInstance().setToolbar(tubusToolbar);

        } catch (IllegalStateException e) {
            // Логируем ошибку, но не падаем
            e.printStackTrace();
        }
    }

    public static void setupBackButton(AppCompatActivity activity, TubusToolbar tubusToolbar) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Обработчик нажатия на кнопку "Назад"
            tubusToolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }

    public static void updateAppName(TubusToolbar tubusToolbar, String newAppName) {
        if (tubusToolbar != null && newAppName != null) {
            tubusToolbar.setAppName(newAppName);
        }
    }
}