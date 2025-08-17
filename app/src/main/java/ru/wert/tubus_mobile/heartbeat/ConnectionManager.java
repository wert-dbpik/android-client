package ru.wert.tubus_mobile.heartbeat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Менеджер соединения с сервером. Обеспечивает подключение, переподключение
 * и управление состоянием соединения.
 */
public class ConnectionManager {

    private static final String TAG = "ConnectionManager";
    private static final String SERVER_ADDRESS = "192.168.2.132";
    private static final int SERVER_PORT = 8080;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 30000;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1000;
    private static final int MAX_RECONNECT_DELAY_MS = 30000;

    private static volatile ConnectionManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageReceiver messageReceiver;
    private HeartbeatManager heartbeatManager;
    private int reconnectAttempts = 0;
    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;
    private ConnectionStatusListener statusListener;

    /**
     * Интерфейс для слушателя изменения состояния соединения.
     */
    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
    }

    private ConnectionManager() {}

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    /**
     * Устанавливает слушателя изменения состояния соединения.
     * @param listener слушатель
     */
    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        Log.d(TAG, "Устанавливается слушатель: " + (listener != null ? "не-null" : "null"));
        this.statusListener = listener;
    }

    /**
     * Запускает сервис соединения.
     */
    public void start() {
        if (isRunning) return;

        isRunning = true;
        new Thread(this::connectLoop).start();
    }

    /**
     * Останавливает сервис соединения.
     */
    public void stop() {
        isRunning = false;
        closeResources();
    }

    private void connectLoop() {
        while (isRunning) {
            try {
                connect();
                startComponents();
                waitWhileConnected();
            } catch (Exception e) {
                handleConnectionError(e);
            } finally {
                cleanupAndScheduleReconnect();
            }
        }
        Log.i(TAG, "Сервис соединения остановлен");
    }

    private void connect() throws IOException {
        Log.i(TAG, "Попытка подключения к серверу...");
        socket = new Socket();
        socket.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT), CONNECT_TIMEOUT_MS);
        socket.setSoTimeout(SOCKET_TIMEOUT_MS);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Log.i(TAG, "Подключение к серверу установлено");
        updateConnectionStatus(true);
        reconnectAttempts = 0;
    }

    private void startComponents() {
        messageReceiver = new MessageReceiver(in);
        heartbeatManager = new HeartbeatManager(out);

        messageReceiver.start();
        heartbeatManager.start();
    }

    private void waitWhileConnected() throws InterruptedException {
        while (isRunning && socket != null && socket.isConnected() && !socket.isClosed()) {
            Thread.sleep(1000);
        }
    }

    void handleConnectionError(Exception e) {
        Log.e(TAG, "Ошибка соединения: " + e.getMessage());
        updateConnectionStatus(false);
    }

    void updateConnectionStatus(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            Log.i(TAG, "Статус соединения изменен: " + (connected ? "подключено" : "отключено"));

            if (statusListener != null) {
                statusListener.onConnectionStatusChanged(connected);
            }
        }
    }

    private void cleanupAndScheduleReconnect() {
        closeResources();

        if (isRunning) {
            int delay = calculateReconnectDelay();
            Log.i(TAG, "Попытка переподключения через " + delay + " мс...");

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int calculateReconnectDelay() {
        reconnectAttempts++;
        return Math.min(INITIAL_RECONNECT_DELAY_MS * (1 << Math.min(reconnectAttempts, 10)),
                MAX_RECONNECT_DELAY_MS);
    }

    private void closeResources() {
        if (heartbeatManager != null) {
            heartbeatManager.stop();
            heartbeatManager = null;
        }

        if (messageReceiver != null) {
            messageReceiver.stop();
            messageReceiver = null;
        }

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            Log.i(TAG, "Ресурсы соединения закрыты");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при закрытии ресурсов: " + e.getMessage());
        }

        updateConnectionStatus(false);
    }
}
