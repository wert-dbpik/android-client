package ru.wert.tubus_mobile.heartbeat;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Класс для управления отправкой heartbeat-сообщений на сервер.
 * Обеспечивает периодическую отправку сообщений для поддержания соединения.
 */
public class HeartbeatManager {

    private static final String TAG = "HeartbeatManager";
    private static final int HEARTBEAT_INTERVAL_MS = 15000; // 15 секунд

    private final PrintWriter out;
    private final ScheduledExecutorService executor;
    private volatile boolean isRunning = false;

    /**
     * Конструктор.
     * @param out выходной поток для отправки сообщений
     */
    public HeartbeatManager(PrintWriter out) {
        this.out = out;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Запускает отправку heartbeat-сообщений.
     */
    public void start() {
        if (isRunning) return;

        isRunning = true;
        executor.scheduleAtFixedRate(this::sendHeartbeat,
                HEARTBEAT_INTERVAL_MS,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        Log.d(TAG, "Heartbeat отправка запущена");
    }

    /**
     * Останавливает отправку heartbeat-сообщений.
     */
    public void stop() {
        if (!isRunning) return;

        isRunning = false;
        executor.shutdownNow();
        Log.d(TAG, "Heartbeat отправка остановлена");
    }

    private void sendHeartbeat() {
        try {
            if (out != null && !out.checkError()) {
                out.println("HEARTBEAT");
                out.flush();
                Log.d(TAG, "Heartbeat отправлен");
            } else {
                Log.w(TAG, "Ошибка отправки heartbeat: выходной поток недоступен");
                ConnectionManager.getInstance().handleConnectionError(new IOException("Поток недоступен"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка отправки heartbeat: " + e.getMessage());
            ConnectionManager.getInstance().handleConnectionError(e);
        }
    }
}
