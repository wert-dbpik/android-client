package ru.wert.bazapik_mobile.data.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.filterList;

public class DownloadFileTask extends AsyncTask<String, String, Boolean> {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

    private final WeakReference<Context> context;
    private final WeakReference<String> fromFolder, destDir;
    private final ProgressDialog mProgressDialog;

    public DownloadFileTask(Context context, String fromFolder, String destDir) {
        this.context = new WeakReference<>(context);
        this.fromFolder = new WeakReference<>(fromFolder);
        this.destDir = new WeakReference<>(destDir);

        mProgressDialog = new ProgressDialog(this.context.get());
        mProgressDialog.setTitle("Минуточку!");
        mProgressDialog.setMessage("Файл загружается...");
        mProgressDialog.setProgressNumberFormat("");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(dialog -> {
            this.cancel(true);
        });
        mProgressDialog.show();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgress(DIALOG_DOWNLOAD_PROGRESS);
    }

    @Override
    protected Boolean doInBackground(String... fileNames) {

            int count;

            try {

                URL url = new URL(DATA_BASE_URL + "/drafts/download/" + fromFolder.get() + "/" + fileNames[0]);

                //Перед загрузкой проверяем наличие уже загруженного ранее файла
                File destFile = new File(destDir.get() + "/" + fileNames[0]);
                if (destFile.exists())
                    destFile.delete();

                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lengthOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lengthOfFile);

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(destDir.get() + "/" + fileNames[0]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                return false;
            }

        return true;

    }

    @Override
    protected void onProgressUpdate(String... progress) {

        int val = Integer.parseInt(progress[0]);
        mProgressDialog.setProgress(val);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mProgressDialog.dismiss();
        new WarningDialog1().show(context.get(),"",
                "Загрузка прервана пользователем");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mProgressDialog.dismiss();
        if (result)
            new WarningDialog1().show(context.get(), "Поздравляю!",
                    "Загрузка прошла успешно!");
        else
            new WarningDialog1().show(context.get(), "Ошибка!",
                    "Что-то пошло не так и загрузка сорвалась! " +
                            "Возможно, потерялась связь с сервером. " +
                            "Повторите попытку позднее.");
    }



}
