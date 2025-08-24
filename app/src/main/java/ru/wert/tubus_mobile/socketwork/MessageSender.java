package ru.wert.tubus_mobile.socketwork;

import android.util.Log;


import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ru.wert.tubus_mobile.socketwork.model.Message;
import ru.wert.tubus_mobile.socketwork.model.MessageType;

/**
 * Класс для отправки сообщений на сервер.
 * Обеспечивает очередь сообщений и heartbeat-пакеты.
 */
public class MessageSender {
    private static final String TAG = "MessageSender";
    private static final int HEARTBEAT_INTERVAL_MS = 15000;

    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final PrintWriter out;
    private volatile boolean running = true;

    public MessageSender(PrintWriter out) {
        this.out = out;
        startHeartbeatSender();
    }

    public void start() {
        new Thread(this::sendMessages).start();
    }

    private void sendMessages() {
        while (running) {
            try {
                Message message = messageQueue.take();
                sendMessageToServer(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "Поток отправки прерван");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка отправки: " + e.getMessage());
            }
        }
        Log.i(TAG, "Поток отправки остановлен");
    }

    private void startHeartbeatSender() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                    sendHeartbeat();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void sendHeartbeat() {
        Message heartbeat = new Message();
        heartbeat.setType(MessageType.HEARTBEAT);
        sendMessageToServer(heartbeat);
    }

    private void sendMessageToServer(Message message) {
        try {
            if (out != null && !out.checkError()) {
                String json = MessageSerializer.serialize(message);
                out.println(json);
                out.flush();

                if (!message.getType().equals(MessageType.HEARTBEAT)) {
                    Log.i(TAG, "Отправлено: " + json);
                }
            } else {
                handleSendError(message);
            }
        } catch (Exception e) {
            handleSendError(message);
        }
    }

    private void handleSendError(Message message) {
        Log.w(TAG, "Ошибка отправки: " + message.toString());
        if (running) {
            requeueMessage(message);
            SocketService.getInstance().stop();
            SocketService.getInstance().start();
        }
    }

    private void requeueMessage(Message message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendMessage(Message message) {
        try {
            messageQueue.put(message);
            Log.d(TAG, "Добавлено в очередь: " + message.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
    }
}
