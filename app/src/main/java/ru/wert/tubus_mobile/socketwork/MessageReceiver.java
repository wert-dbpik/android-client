package ru.wert.tubus_mobile.socketwork;

import android.util.Log;

import ru.wert.tubus_mobile.socketwork.model.Message;
import ru.wert.tubus_mobile.socketwork.model.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Класс для приема сообщений от сервера с улучшенной обработкой ошибок.
 * Обрабатывает входящие сообщения и heartbeat-пакеты.
 */
public class MessageReceiver {
    private static final String TAG = "MessageReceiver";
    private static final long HEARTBEAT_TIMEOUT_MS = 30000;

    private final BufferedReader in;
    private volatile boolean running = true;
    private long lastHeartbeatTime = System.currentTimeMillis();

    public MessageReceiver(BufferedReader in) {
        this.in = in;
    }

    public void start() {
        new Thread(this::receiveMessages).start();
    }

    private void receiveMessages() {
        while (running) {
            try {
                processIncomingMessage();
                checkHeartbeatTimeout();
            } catch (SocketTimeoutException e) {
                handleSocketTimeout();
            } catch (IOException e) {
                handleIOException(e);
            } catch (Exception e) {
                handleUnexpectedError(e);
            }
        }
        Log.i(TAG, "Поток получения сообщений остановлен");
    }

    private void processIncomingMessage() throws IOException {
        String serverMessage = in.readLine();
        if (serverMessage != null) {
            logRawMessage(serverMessage);

            // Анализируем сообщение перед десериализацией
            if (!MessageSerializer.isValidJsonObject(serverMessage)) {
                String errorAnalysis = ErrorAnalyzer.analyzeError(serverMessage);
                Log.w(TAG, "Получена ошибка от сервера: " + errorAnalysis);
                handleServerError(errorAnalysis);
                return;
            }

            Message message = MessageSerializer.deserialize(serverMessage);

            if (isValidMessage(message)) {
                if (message.getType() == MessageType.HEARTBEAT) {
                    handleHeartbeat();
                } else {
                    handleRegularMessage(message);
                }
            } else {
                Log.w(TAG, "Получено невалидное сообщение, пропускаем обработку");
            }
        }
    }

    private void handleServerError(String errorDescription) {
        Log.w(TAG, "Обработка ошибки сервера: " + errorDescription);
        // Можно добавить дополнительную логику обработки ошибок
    }

    /**
     * Логгирует сырое сообщение для отладки
     * @param rawMessage сырое сообщение от сервера
     */
    private void logRawMessage(String rawMessage) {
        if (rawMessage != null && rawMessage.length() > 200) {
            Log.d(TAG, "Получено сырое сообщение: " + rawMessage.substring(0, 200) + "...");
        } else {
            Log.d(TAG, "Получено сырое сообщение: " + rawMessage);
        }
    }

    /**
     * Проверяет валидность сообщения
     * @param message сообщение для проверки
     * @return true если сообщение валидно
     */
    private boolean isValidMessage(Message message) {
        return message != null && message.getType() != null;
    }

    private void handleHeartbeat() {
        lastHeartbeatTime = System.currentTimeMillis();
        Log.d(TAG, "Получен HEARTBEAT от сервера");
        SocketService.getInstance().updateConnectionStatus(true);
    }

    private void handleRegularMessage(Message message) {
        Log.d(TAG, "Сообщение получено: " + message.toString());
        // Здесь можно добавить обработку различных типов сообщений
    }

    private void checkHeartbeatTimeout() {
        if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT_MS) {
            Log.w(TAG, "Превышен таймаут HEARTBEAT (30 сек)");
            initiateReconnection();
        }
    }

    private void handleSocketTimeout() {
        if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT_MS) {
            Log.w(TAG, "Таймаут соединения");
            initiateReconnection();
        }
    }

    private void handleIOException(IOException e) {
        if (running) {
            Log.e(TAG, "Ошибка ввода-вывода: " + e.getMessage());
            initiateReconnection();
        }
    }

    private void handleUnexpectedError(Exception e) {
        Log.e(TAG, "Неожиданная ошибка в receiveMessages: " + e.getMessage(), e);
        if (running) {
            initiateReconnection();
        }
    }

    private void initiateReconnection() {
        SocketService.getInstance().updateConnectionStatus(false);
        SocketService.getInstance().stop();
        SocketService.getInstance().start();
        running = false;
    }

    public void stop() {
        running = false;
    }
}