package ru.wert.tubus_mobile.socketwork;

import android.util.Log;
import ru.wert.tubus_mobile.ThisApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Менеджер сокет-соединения
 */
public class SocketConnectionManager {
    private static final String TAG = "SocketConnectionManager";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect() throws IOException {
        String serverAddress = ThisApplication.getProp("IP");
        String serverPortStr = ThisApplication.getProp("PORT");

        if (serverAddress == null || serverPortStr == null) {
            throw new IOException("Не заданы адрес или порт сервера");
        }

        int serverPort = Integer.parseInt(serverPortStr);

        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, serverPort), CONNECT_TIMEOUT_MS);
        socket.setSoTimeout(SOCKET_TIMEOUT_MS);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        Log.i(TAG, "Подключение к серверу " + serverAddress + ":" + serverPort + " установлено");
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            Log.i(TAG, "Ресурсы сокета закрыты");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }
}