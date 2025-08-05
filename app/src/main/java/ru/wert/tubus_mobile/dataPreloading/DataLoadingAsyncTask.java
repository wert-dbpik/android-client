package ru.wert.tubus_mobile.dataPreloading;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.models.*;
import ru.wert.tubus_mobile.organizer.OrganizerActivity;

public class DataLoadingAsyncTask extends AsyncTask<Void, String, Boolean> {
    private static final String TAG = "DataLoadingTask";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final Activity activity;
    private final TextView tvLoadingStatus;
    private int retryCount = 0;

    public DataLoadingAsyncTask(Activity activity) {
        this.activity = activity;
        this.tvLoadingStatus = activity.findViewById(R.id.tvLoadingStatus);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.runOnUiThread(() -> {
            if (tvLoadingStatus != null) {
                tvLoadingStatus.setText("Подготовка к загрузке...");
            }
        });
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            return attemptDataLoading();
        } catch (Exception e) {
            Log.e(TAG, "Critical error during data loading", e);
            publishProgress("Критическая ошибка");
            return false;
        }
    }

    private Boolean attemptDataLoading() throws IOException {
        try {
            publishProgress("Установка соединения...");
            BatchResponse response = BatchService.loadInitialData();

            if (response == null) {
                throw new IOException("Пустой ответ от сервера");
            }

            processResponseWithProgress(response);
            return true;

        } catch (IOException e) {
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                Log.w(TAG, "Attempt " + retryCount + " failed, retrying...", e);
                publishProgress("Повторная попытка " + retryCount + "/" + MAX_RETRIES);

                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }

                return attemptDataLoading();
            }
            throw e;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (tvLoadingStatus != null && values.length > 0) {
            tvLoadingStatus.setText(values[0]);
        }
    }

    // ... остальные методы без изменений ...
    private void processResponseWithProgress(BatchResponse response) {
        String[] steps = {
                "пользователей", "чаты", "группы изделий",
                "чертежи", "комплекты", "паспорта"
        };

        Runnable[] processors = {
                () -> processUsers(response.getUsers()),
                () -> processRooms(response.getRooms()),
                () -> processProductGroups(response.getProductGroups()),
                () -> processDrafts(response.getDrafts()),
                () -> processFolders(response.getFolders()),
                () -> processPassports(response.getPassports())
        };

        for (int i = 0; i < steps.length; i++) {
            publishProgress("Загружаем " + steps[i] + "...");
            processors[i].run();
        }
    }

    private void processUsers(List<User> users) {
        if (users != null) {
            ThisApplication.LIST_OF_ALL_USERS = users.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processRooms(List<Room> rooms) {
        if (rooms != null) {
            ThisApplication.LIST_OF_ALL_ROOMS = rooms.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processProductGroups(List<ProductGroup> productGroups) {
        if (productGroups != null) {
            ThisApplication.LIST_OF_ALL_PRODUCT_GROUPS = productGroups.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processDrafts(List<Draft> drafts) {
        if (drafts != null) {
            ThisApplication.LIST_OF_ALL_DRAFTS = drafts.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processFolders(List<Folder> folders) {
        if (folders != null) {
            ThisApplication.LIST_OF_ALL_FOLDERS = folders.stream()
                    .sorted(ThisApplication.usefulStringComparator())
                    .collect(Collectors.toList());
        }
    }

    private void processPassports(List<Passport> passports) {
        if (passports != null) {
            ThisApplication.LIST_OF_ALL_PASSPORTS = passports.stream()
                    .sorted(ThisApplication.passportsComparatorDetailFirst())
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            startOrganizerActivity();
        } else {
            showRetryDialog();
        }
    }

    private void startOrganizerActivity() {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(activity, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    private void showRetryDialog() {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle("Ошибка загрузки")
                .setMessage("Не удалось загрузить данные. Проверьте подключение к интернету.")
                .setPositiveButton("Повторить", (d, w) -> retryLoading())
                .setNegativeButton("Выйти", (d, w) -> activity.finish())
                .setCancelable(false)
                .show());
    }

    private void retryLoading() {
        new DataLoadingAsyncTask(activity).execute();
    }
}
