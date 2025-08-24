package ru.wert.tubus_mobile.socketwork;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.wert.tubus_mobile.CurrentUser;
import ru.wert.tubus_mobile.socketwork.model.Message;
import ru.wert.tubus_mobile.socketwork.model.MessageType;

/**
 * Сервис для управления сокет-соединением с чат-сервером.
 * Обеспечивает подключение, переподключение и обмен сообщениями.
 */
public class SocketService {
    private static final String TAG = "SocketService";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 30000;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1000;
    private static final int MAX_RECONNECT_DELAY_MS = 30000;

    private static SocketService instance;
    private SocketConnectionManager connectionManager;
    private MessageReceiver messageReceiver;
    private MessageSender messageSender;

    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private int reconnectAttempts = 0;

    private ConnectionStatusListener statusListener;

    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
    }

    private SocketService() {
        connectionManager = new SocketConnectionManager();
    }

    public static synchronized SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * Запускает сервис сокета
     */
    public void start() {
        if (running.get()) {
            Log.d(TAG, "Сервис сокета уже запущен");
            return;
        }

        running.set(true);
        Log.i(TAG, "Запуск сервиса сокета");

        new Thread(this::connectLoop).start();
    }

    /**
     * Останавливает сервис сокета
     */
    public void stop() {
        running.set(false);
        Log.i(TAG, "Остановка сервиса сокета");

        sendMessageUserOut();
        stopComponents();
    }

    /**
     * Отправляет сообщение на сервер
     */
    public void sendMessage(Message message) {
        if (messageSender != null) {
            messageSender.sendMessage(message);
        } else {
            Log.e(TAG, "Не удалось отправить сообщение, messageSender = null");
        }
    }

    /**
     * Отправляет сообщение о входе пользователя
     */
    public static void sendMessageUserIn() {
        try {
            Message userInMessage = new Message();
            userInMessage.setType(MessageType.USER_IN);
            userInMessage.setSenderId(CurrentUser.getInstance().getUser().getId());
            getInstance().sendMessage(userInMessage);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отправке USER_IN: " + e.getMessage());
        }
    }

    /**
     * Отправляет сообщение о выходе пользователя
     */
    public static void sendMessageUserOut() {
        try {
            Message userOutMessage = new Message();
            userOutMessage.setType(MessageType.USER_OUT);
            userOutMessage.setSenderId(CurrentUser.getInstance().getUser().getId());
            getInstance().sendMessage(userOutMessage);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отправке USER_OUT: " + e.getMessage());
        }
    }

    /**
     * Основной цикл подключения
     */
    private void connectLoop() {
        while (running.get()) {
            try {
                connectToServer();
                waitWhileConnected();
            } catch (Exception e) {
                handleConnectionError(e);
            } finally {
                cleanupAndScheduleReconnect();
            }
        }
        Log.i(TAG, "Сервис сокета остановлен");
    }

    private void connectToServer() throws IOException {
        Log.i(TAG, "Попытка подключения к серверу...");
        connectionManager.connect();

        messageReceiver = new MessageReceiver(connectionManager.getIn());
        messageSender = new MessageSender(connectionManager.getOut());

        messageReceiver.start();
        messageSender.start();

        sendMessageUserIn();
        Log.i(TAG, "Сокет успешно подключен, потоки запущены");
        updateConnectionStatus(true);
        reconnectAttempts = 0;

        // Проверяем, что соединение действительно установлено
        if (connectionManager.isConnected()) {
            Log.i(TAG, "Сокет успешно подключен, проверяем работоспособность...");

            // Отправляем тестовый heartbeat для проверки соединения
            try {
                Message testMessage = new Message();
                testMessage.setType(MessageType.HEARTBEAT);
                String testJson = MessageSerializer.serialize(testMessage);

                PrintWriter out = connectionManager.getOut();
                if (out != null && !out.checkError()) {
                    out.println(testJson);
                    out.flush();
                    Log.d(TAG, "Тестовый heartbeat отправлен");
                }
            } catch (Exception e) {
                Log.w(TAG, "Ошибка при отправке тестового heartbeat: " + e.getMessage());
            }
        }
    }

    private void waitWhileConnected() throws InterruptedException {
        while (running.get() && connectionManager.isConnected()) {
            Thread.sleep(1000);
        }
    }

    private void handleConnectionError(Exception e) {
        Log.e(TAG, "Ошибка подключения: " + e.getMessage());
        updateConnectionStatus(false);
    }

    private void cleanupAndScheduleReconnect() {
        stopComponents();

        if (running.get()) {
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

    private void stopComponents() {
        if (messageReceiver != null) {
            messageReceiver.stop();
            messageReceiver = null;
        }
        if (messageSender != null) {
            messageSender.stop();
            messageSender = null;
        }
        connectionManager.close();
    }

    public void updateConnectionStatus(boolean connected) {
        if (statusListener != null) {
            statusListener.onConnectionStatusChanged(connected);
        }
    }

    public boolean isConnected() {
        return connectionManager.isConnected();
    }
}
