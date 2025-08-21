package ru.wert.tubus_mobile.heartbeat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Класс для приема сообщений от сервера.
 * Обрабатывает входящие сообщения и heartbeat-пакеты.
 */
public class MessageReceiver {

    private static final String TAG = "MessageReceiver";
    private static final long HEARTBEAT_TIMEOUT_MS = 30000; // 30 секунд

    private final BufferedReader in;
    private volatile boolean isRunning = false;
    private long lastHeartbeatTime = System.currentTimeMillis();

    /**
     * Конструктор.
     * @param in входной поток для чтения сообщений от сервера
     */
    public MessageReceiver(BufferedReader in) {
        this.in = in;
    }

    /**
     * Запускает поток для приема сообщений.
     */
    public void start() {
        if (isRunning) return;

        isRunning = true;
        new Thread(this::receiveMessages).start();
        Log.d(TAG, "Прием сообщений запущен");
    }

    private void receiveMessages() {
        while (isRunning) {
            try {
                String message = in.readLine();
                if (message != null) {
                    processMessage(message);
                }
                checkHeartbeatTimeout();
            } catch (SocketTimeoutException e) {
                handleSocketTimeout();
            } catch (IOException e) {
                handleIOException(e);
            }
        }
        Log.i(TAG, "Поток получения сообщений остановлен");
    }

    private void processMessage(String message) {
        if ("HEARTBEAT".equals(message)) {
            handleHeartbeat();
        } else {
            Log.d(TAG, "Получено сообщение: " + message);
        }
    }

    private void handleHeartbeat() {
        lastHeartbeatTime = System.currentTimeMillis();
        Log.d(TAG, "Получен HEARTBEAT от сервера");
        ConnectionManager.getInstance().updateConnectionStatus(true);
    }

    private void checkHeartbeatTimeout() {
        if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT_MS) {
//            Log.w(TAG, "Превышен таймаут HEARTBEAT (30 сек)");
            ConnectionManager.getInstance().handleConnectionError(new IOException("Таймаут heartbeat"));
        }
    }

    private void handleSocketTimeout() {
        if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT_MS) {
            Log.w(TAG, "Таймаут соединения");
            ConnectionManager.getInstance().handleConnectionError(new IOException("Таймаут сокета"));
        }
    }

    private void handleIOException(IOException e) {
        if (isRunning) {
//            Log.e(TAG, "Ошибка соединения: " + e.getMessage());
            ConnectionManager.getInstance().handleConnectionError(e);
        }
    }

    /**
     * Останавливает прием сообщений.
     */
    public void stop() {
        isRunning = false;
    }
}
