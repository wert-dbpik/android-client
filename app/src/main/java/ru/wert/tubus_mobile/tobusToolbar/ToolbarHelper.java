package ru.wert.tubus_mobile.tobusToolbar;

import androidx.appcompat.app.AppCompatActivity;

import ru.wert.tubus_mobile.socketwork.SocketService;

/**
 * Вспомогательный класс для настройки кастомного Toolbar.
 * Обеспечивает интеграцию с SocketService для отображения статуса соединения.
 */
public class ToolbarHelper {

    /**
     * Настраивает Toolbar с указанным именем приложения
     * @param activity текущая активность
     * @param tubusToolbar кастомный Toolbar
     * @param appName имя приложения для отображения
     */
    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar, String appName) {
        setupToolbar(activity, tubusToolbar);
        updateAppName(tubusToolbar, appName);
    }

    /**
     * Основной метод настройки Toolbar
     * @param activity текущая активность
     * @param tubusToolbar кастомный Toolbar
     */
    public static void setupToolbar(AppCompatActivity activity, TubusToolbar tubusToolbar) {
        if (tubusToolbar == null) {
            return;
        }

        try {
            // Устанавливаем кастомный Toolbar как ActionBar
            activity.setSupportActionBar(tubusToolbar);

            // Скрываем стандартный заголовок
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Регистрируем слушатель статуса соединения в SocketService
            registerConnectionStatusListener(tubusToolbar);

            // Устанавливаем начальный статус (красный, пока не подключимся)
            tubusToolbar.showServerStatus(false);

        } catch (IllegalStateException e) {
            // Логируем ошибку, но не прерываем работу приложения
            e.printStackTrace();
        }
    }

    /**
     * Регистрирует слушатель изменения статуса соединения в SocketService
     * @param tubusToolbar Toolbar для обновления статуса
     */
    private static void registerConnectionStatusListener(TubusToolbar tubusToolbar) {
        SocketService socketService = SocketService.getInstance();

        socketService.setConnectionStatusListener(new SocketService.ConnectionStatusListener() {
            @Override
            public void onConnectionStatusChanged(boolean isConnected) {
                // Обновляем статус в основном потоке
                if (tubusToolbar != null) {
                    tubusToolbar.post(() -> {
                        tubusToolbar.showServerStatus(isConnected);
                    });
                }
            }
        });
    }

    /**
     * Настраивает кнопку "Назад" в Toolbar
     * @param activity текущая активность
     * @param tubusToolbar кастомный Toolbar
     */
    public static void setupBackButton(AppCompatActivity activity, TubusToolbar tubusToolbar) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Обработчик нажатия на кнопку "Назад"
            tubusToolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }

    /**
     * Обновляет имя приложения в Toolbar
     * @param tubusToolbar кастомный Toolbar
     * @param newAppName новое имя приложения
     */
    public static void updateAppName(TubusToolbar tubusToolbar, String newAppName) {
        if (tubusToolbar != null && newAppName != null) {
            tubusToolbar.setAppName(newAppName);
        }
    }

    /**
     * Обновляет статус соединения в Toolbar вручную
     * @param tubusToolbar кастомный Toolbar
     * @param isConnected статус соединения (true - подключено, false - отключено)
     */
    public static void updateConnectionStatus(TubusToolbar tubusToolbar, boolean isConnected) {
        if (tubusToolbar != null) {
            tubusToolbar.showServerStatus(isConnected);
        }
    }

    /**
     * Сбрасывает слушатель статуса соединения
     * Используется при смене активности или уничтожении приложения
     */
    public static void resetConnectionStatusListener() {
        SocketService.getInstance().setConnectionStatusListener(null);
    }
}