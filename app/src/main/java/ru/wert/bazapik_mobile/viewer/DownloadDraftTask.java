package ru.wert.bazapik_mobile.viewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadDraftTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "Загрузка файла в фоне";

    private Context context;
    private PowerManager.WakeLock mWakeLock;

    public DownloadDraftTask(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        String remoteFile = sUrl[0];
        String localFile = sUrl[1];

        try {
            URL url = new URL(remoteFile);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            //Возвращаем сообщение об ошибке, если что-то пошло не так
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            //скачиваем файл
            input = connection.getInputStream();

            output = new FileOutputStream(localFile);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // отменить загрузку при нажатии на кнопку назад
                if (isCancelled()) {
                    input.close();
                    Log.d(TAG, "Загрузка отменена пользователем");
                    return null;
                }
                total += count;
                output.write(data, 0, count);
            }

        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }


        return "OK";
    }

}
