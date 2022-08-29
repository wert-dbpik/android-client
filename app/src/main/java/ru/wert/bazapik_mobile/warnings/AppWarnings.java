package ru.wert.bazapik_mobile.warnings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.util.DownloadFileTask;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;

import static ru.wert.bazapik_mobile.ThisApplication.APPLICATION_VERSION_AVAILABLE;


public class AppWarnings {

    public static void showAlert_NoAppVersionsAvailable(Context context){
        new AlertDialog.Builder(context)
                .setTitle("ВНИМАНИЕ!")
                .setMessage("Нет информации о доступных версиях программы")
                .setPositiveButton(R.string.OK, (arg0, arg1) -> {
                    //Просто ждать
                }).create().show();
    }

    public static void showAlert_NewAppVersionAvailable(Context context){
        new AlertDialog.Builder(context)
                .setTitle("ВНИМАНИЕ!")
                .setMessage(String.format("Версия установленной программы - %s. Доступна новая версия 'BazaPIK-%s.apk'. " +
                                "Для обновления программы загрузите установочный файл apk и устновите программу самостоятельно. " +
                                "Загрузить новую версию в папку 'Загрузки'? "
                        , ThisApplication.APPLICATION_VERSION, APPLICATION_VERSION_AVAILABLE))
                .setNegativeButton(R.string.later, null)
                .setPositiveButton(R.string.download, (arg0, arg1) -> {
                    String fileName = "BazaPIK-" + APPLICATION_VERSION_AVAILABLE + ".apk";

                    File destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    AsyncTask<String, String, Boolean> downloadTask = new DownloadFileTask(
                            context,
                            "apk",
                            destinationFolder.toString());
                    downloadTask.execute(fileName);

                }).create().show();
    }
}

