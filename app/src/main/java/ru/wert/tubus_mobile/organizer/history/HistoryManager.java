package ru.wert.tubus_mobile.organizer.history;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String HISTORY_FILE = "history.txt";
    private static final int MAX_HISTORY_ITEMS = 20;
    private final Context context;

    public HistoryManager(Context context) {
        this.context = context;
    }

    public synchronized void addToHistory(String drawingNumber) {
        if (drawingNumber == null || drawingNumber.trim().isEmpty()) return;

        List<String> history = loadHistoryFromFile(); // Всегда читаем из файла
        history.removeIf(item -> item.equals(drawingNumber));
        history.add(0, drawingNumber);

        while (history.size() > MAX_HISTORY_ITEMS) {
            history.remove(history.size() - 1);
        }

        saveHistory(history); // Просто сохраняем
    }

    public synchronized List<String> getHistory() {
        return loadHistoryFromFile(); // Всегда читаем из файла
    }

    private synchronized List<String> loadHistoryFromFile() {
        List<String> history = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                try (FileInputStream fis = context.openFileInput(HISTORY_FILE);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        history.add(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history; // Возвращаем напрямую
    }

    private synchronized void saveHistory(List<String> history) {
        try (FileOutputStream fos = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            for (String item : history) {
                writer.write(item + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void clearHistory() {
        try (FileOutputStream fos = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE)) {
            fos.write("".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
