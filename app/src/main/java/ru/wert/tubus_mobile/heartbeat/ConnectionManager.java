package ru.wert.tubus_mobile.heartbeat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.tobusToolbar.TubusToolbar;

/**
 * Менеджер соединения с сервером. Обеспечивает подключение, переподключение
 * и управление состоянием соединения. Работает на уровне всего приложения.
 */
public class ConnectionManager {

    private static final String TAG = "ConnectionManager";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 30000;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1000;
    private static final int MAX_RECONNECT_DELAY_MS = 30000;
    private static final int HEARTBEAT_INTERVAL_MS = 15000; // 15 секунд

    private static volatile ConnectionManager instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageReceiver messageReceiver;

    private int reconnectAttempts = 0;
    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;

    private ConnectionStatusListener statusListener;
    private TubusToolbar toolbar;

    /**
     * Интерфейс для слушателя изменения состояния соединения.
     */
    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
    }

    private ConnectionManager() {
        // Приватный конструктор для синглтона
    }

    /**
     * Возвращает единственный экземпляр ConnectionManager.
     * @return экземпляр ConnectionManager
     */
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
     * Устанавливает тулбар для отображения статуса соединения.
     * @param toolbar тулбар
     */
    public void setToolbar(TubusToolbar toolbar) {
        this.toolbar = toolbar;
    }

    /**
     * Запускает сервис соединения с сервером.
     * Должен вызываться при старте приложения.
     */
    public void start() {
        if (isRunning) {
            Log.d(TAG, "Сервис соединения уже запущен");
            return;
        }

        isRunning = true;
        Log.i(TAG, "Запуск сервиса соединения");
        new Thread(this::connectLoop).start();
    }

    /**
     * Останавливает сервис соединения.
     * Должен вызываться при завершении приложения.
     */
    public void stop() {
        isRunning = false;
        Log.i(TAG, "Остановка сервиса соединения");
        closeResources();
    }

    /**
     * Перезапускает сервис соединения с новыми параметрами.
     * Используется при смене сервера.
     */
    public void restart() {
        Log.i(TAG, "Перезапуск сервиса соединения");
        stop();
        try {
            Thread.sleep(1000); // Даем время для корректного завершения
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        start();
    }

    /**
     * Основной цикл подключения и поддержания соединения.
     */
    private void connectLoop() {
        while (isRunning) {
            try {
                connectToServer();
                startCommunicationComponents();
                waitWhileConnected();
            } catch (Exception e) {
                handleConnectionError(e);
            } finally {
                cleanupAndScheduleReconnect();
            }
        }
        Log.i(TAG, "Сервис соединения остановлен");
    }

    /**
     * Подключается к серверу используя текущие настройки из ThisApplication.
     * @throws IOException если подключение не удалось
     */
    private void connectToServer() throws IOException {
        String serverAddress = ThisApplication.getProp("IP");
        String serverPort = ThisApplication.getProp("PORT");

        if (serverAddress == null || serverAddress.isEmpty() ||
                serverPort == null || serverPort.isEmpty()) {
            throw new IOException("Не заданы адрес или порт сервера");
        }

        Log.i(TAG, "Попытка подключения к серверу: " + serverAddress + ":" + serverPort);

        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, Integer.parseInt(serverPort)),
                CONNECT_TIMEOUT_MS);
        socket.setSoTimeout(SOCKET_TIMEOUT_MS);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Log.i(TAG, "Подключение к серверу установлено: " + serverAddress + ":" + serverPort);
        updateConnectionStatus(true);
        reconnectAttempts = 0;
    }

    /**
     * Запускает компоненты для обмена сообщениями.
     */
    private void startCommunicationComponents() {
        messageReceiver = new MessageReceiver(in);
        messageReceiver.start();

        // Запускаем отправку heartbeat
        startHeartbeat();
    }

    /**
     * Запускает периодическую отправку heartbeat-сообщений.
     */
    private void startHeartbeat() {
        new Thread(() -> {
            while (isRunning && isConnected && out != null && !out.checkError()) {
                try {
                    sendHeartbeat();
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка отправки heartbeat: " + e.getMessage());
                    handleConnectionError(e);
                    break;
                }
            }
        }).start();
    }

    /**
     * Отправляет heartbeat-сообщение на сервер.
     */
    private void sendHeartbeat() {
        if (out != null && !out.checkError()) {
            out.println("HEARTBEAT");
            out.flush();
            Log.d(TAG, "Heartbeat отправлен");
        }
    }

    /**
     * Ожидает пока соединение активно.
     * @throws InterruptedException если поток прерван
     */
    private void waitWhileConnected() throws InterruptedException {
        while (isRunning && socket != null && socket.isConnected() && !socket.isClosed()) {
            Thread.sleep(1000);
        }
    }

    /**
     * Обрабатывает ошибки соединения.
     * @param e исключение
     */
    void handleConnectionError(Exception e) {
        Log.e(TAG, "Ошибка соединения: " + e.getMessage());
        updateConnectionStatus(false);
    }

    /**
     * Обновляет статус соединения и уведомляет слушателей.
     * @param connected true если соединение установлено
     */
    void updateConnectionStatus(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            Log.i(TAG, "Статус соединения изменен: " + (connected ? "подключено" : "отключено"));

            // Обновляем отображение в тулбаре
            updateToolbarStatus(connected);

            // Уведомляем слушателей
            if (statusListener != null) {
                statusListener.onConnectionStatusChanged(connected);
            }
        }
    }

    /**
     * Обновляет отображение статуса соединения в тулбаре.
     * @param connected true если соединение установлено
     */
    private void updateToolbarStatus(boolean connected) {
        if (toolbar != null) {
            if (connected) {
                toolbar.hideAlarm();
            } else {
                toolbar.showAlarm("НЕТ СВЯЗИ");
            }
        }
    }

    /**
     * Очищает ресурсы и планирует переподключение.
     */
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

    /**
     * Вычисляет задержку перед переподключением с экспоненциальным откатом.
     * @return задержка в миллисекундах
     */
    private int calculateReconnectDelay() {
        reconnectAttempts++;
        return Math.min(INITIAL_RECONNECT_DELAY_MS * (1 << Math.min(reconnectAttempts, 10)),
                MAX_RECONNECT_DELAY_MS);
    }

    /**
     * Закрывает все ресурсы соединения.
     */
    private void closeResources() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            Log.i(TAG, "Ресурсы соединения закрыты");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при закрытии ресурсов: " + e.getMessage());
        }

        updateConnectionStatus(false);
    }

    /**
     * Проверяет, установлено ли соединение с сервером.
     * @return true если соединение активно
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Проверяет, запущен ли сервис соединения.
     * @return true если сервис запущен
     */
    public boolean isRunning() {
        return isRunning;
    }
}