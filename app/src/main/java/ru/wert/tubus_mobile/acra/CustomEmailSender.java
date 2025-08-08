package ru.wert.tubus_mobile.acra;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.wert.tubus_mobile.ThisApplication;

public class CustomEmailSender implements ReportSender {
    private static final String TAG = "CustomEmailSender";
    private final Context context;

    public CustomEmailSender(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException {
        try {
            // 1. Подготовка данных
            String user = ThisApplication.getProp("USER_NAME");
            String message = String.format(
                    "Пользователь: %s\nВерсия приложения: %s\n\nСообщение о сбое:\n%s",
                    user,
                    ThisApplication.APPLICATION_VERSION,
                    "Подробности во вложенном файле"
            );

            // 2. Создание файла с отчетом
            File reportFile = createCrashReportFile(report);
            if (reportFile == null || !reportFile.exists()) {
                throw new ReportSenderException("Не удалось создать файл отчета");
            }

            // 3. Получение Uri для файла
            Uri fileUri = getFileUri(context, reportFile);
            Log.d(TAG, "File URI: " + fileUri);

            // 4. Создание и настройка Intent
            Intent emailIntent = createEmailIntent(message, fileUri);

            // 5. Проверка доступности почтового клиента
            if (!isEmailClientAvailable(context, emailIntent)) {
                showToast(context, "Не найден почтовый клиент");
                throw new ReportSenderException("Почтовый клиент не доступен");
            }

            // 6. Запуск в главном потоке
            launchEmailIntent(context, emailIntent);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка отправки отчета", e);
            throw new ReportSenderException("Ошибка отправки: " + e.getMessage(), e);
        }
    }

    private File createCrashReportFile(CrashReportData report) throws IOException {
        File reportsDir = new File(context.getCacheDir(), "crash_reports");
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            Log.e(TAG, "Не удалось создать директорию для отчетов");
            return null;
        }

        String fileName = String.format(
                "crash_report_%s.txt",
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date())
        );

        File reportFile = new File(reportsDir, fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
            writer.println("===== CRASH REPORT =====\n");
            writer.println(report.toString());
            return reportFile;
        }
    }

    private Uri getFileUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка получения Uri для файла", e);
            return null;
        }
    }

    private Intent createEmailIntent(String message, Uri attachmentUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"wert001@yandex.ru"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "TubusMobile Crash Report");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (attachmentUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private boolean isEmailClientAvailable(Context context, Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    private void launchEmailIntent(Context context, Intent intent) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка запуска почтового клиента", e);
                showToast(context, "Ошибка при открытии почтового клиента");
            }
        });
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}


