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
    private List<String> cachedHistory = null;

    public HistoryManager(Context context) {
        this.context = context;
    }

    public synchronized void addToHistory(String drawingNumber) {
        if (drawingNumber == null || drawingNumber.trim().isEmpty()) return;

        List<String> history = getHistory();
        history.removeIf(item -> item.equals(drawingNumber));
        history.add(0, drawingNumber);

        while (history.size() > MAX_HISTORY_ITEMS) {
            history.remove(history.size() - 1);
        }

        saveHistory(history);
    }

    public synchronized List<String> getHistory() {
        if (cachedHistory != null) {
            return new ArrayList<>(cachedHistory);
        }

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

        cachedHistory = new ArrayList<>(history);
        return history;
    }

    private synchronized void saveHistory(List<String> history) {
        cachedHistory = new ArrayList<>(history);
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
        cachedHistory = new ArrayList<>();
        try (FileOutputStream fos = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE)) {
            fos.write("".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
