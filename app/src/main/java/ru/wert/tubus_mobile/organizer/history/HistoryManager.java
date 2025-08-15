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

    // Добавление нового элемента в историю
    public synchronized void addToHistory(String drawingNumber) {
        if (drawingNumber == null || drawingNumber.trim().isEmpty()) return;
        List<String> history = getHistory();
        // Удаляем если уже есть (чтобы не дублировать)
        history.remove(drawingNumber);
        // Добавляем в начало
        history.add(0, drawingNumber);
        // Ограничиваем размер истории
        if (history.size() > MAX_HISTORY_ITEMS) {
            history = history.subList(0, MAX_HISTORY_ITEMS);
        }
        saveHistory(history);
    }

    // Получение всей истории
    public synchronized List<String> getHistory() {
        List<String> history = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                FileInputStream fis = context.openFileInput(HISTORY_FILE);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String line;
                while ((line = reader.readLine()) != null) {
                    history.add(line);
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return history;
    }

    // Сохранение истории в файл
    private void saveHistory(List<String> history) {
        try {
            FileOutputStream fos = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            for (String item : history) {
                writer.write(item + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
