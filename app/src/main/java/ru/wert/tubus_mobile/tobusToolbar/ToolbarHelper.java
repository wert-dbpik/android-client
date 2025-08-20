package ru.wert.tubus_mobile.tobusToolbar;

import androidx.appcompat.app.AppCompatActivity;

public class ToolbarHelper {

    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar, String title) {
        setupToolbar(activity, tubusToolbar, title, null);
    }

    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar,
                                    String title, String subtitle) {
        if (tubusToolbar == null) return;

        try {
            // Устанавливаем кастомный Toolbar как ActionBar
            activity.setSupportActionBar(tubusToolbar);

            // Скрываем стандартный заголовок
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Настраиваем заголовок и подзаголовок
            if (title != null && !title.isEmpty()) {
                tubusToolbar.setTitle(title);
            } else {
                tubusToolbar.hideTitle();
            }

            if (subtitle != null && !subtitle.isEmpty()) {
                tubusToolbar.setSubtitle(subtitle);
            } else {
                tubusToolbar.hideSubtitle();
            }
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

    public static void updateTitle(TubusToolbar tubusToolbar, String newTitle) {
        if (tubusToolbar != null) {
            if (newTitle != null && !newTitle.isEmpty()) {
                tubusToolbar.setTitle(newTitle);
            } else {
                tubusToolbar.hideTitle();
            }
        }
    }

    public static void updateSubtitle(TubusToolbar tubusToolbar, String newSubtitle) {
        if (tubusToolbar != null) {
            if (newSubtitle != null && !newSubtitle.isEmpty()) {
                tubusToolbar.setSubtitle(newSubtitle);
            } else {
                tubusToolbar.hideSubtitle();
            }
        }
    }

    public static void updateAppName(TubusToolbar tubusToolbar, String newAppName) {
        if (tubusToolbar != null && newAppName != null) {
            tubusToolbar.setAppName(newAppName);
        }
    }
}