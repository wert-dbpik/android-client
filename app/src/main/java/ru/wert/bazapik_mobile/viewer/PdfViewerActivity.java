package ru.wert.bazapik_mobile.viewer;

import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;
import static ru.wert.bazapik_mobile.constants.StaticMethods.clearAppCash;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.main.BaseActivity;

public class PdfViewerActivity extends BaseActivity {
    public static final String TAG = "PdfViewerActivity";

    private Long draftId;
    private PDFView pdfView;
    /**
     * Тестовое сообщение на экране
     */
    private TextView tvTest;
    /**
     * Путь к удаленному файлу PDF (на сервере)
     */
    private String remotePdfFile;
    /**
     * Путь к локальному файлу PDF
     */
    private File localPdfFile;
    int oldOrientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
//        tvTest = findViewById(R.id.tvTest);
        pdfView = findViewById(R.id.pdfView);

        //Из интента получаем id чертежа
        draftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));

//        String dbdir = "http://192.168.1.84:8080/drafts/download/drafts/";
        String dbdir = Consts.DATA_BASE_URL + "drafts/download/drafts/";

        remotePdfFile = dbdir + draftId + ".pdf";

        localPdfFile = new File(TEMP_DIR + "/" + draftId + ".pdf");

        showPDF();

    }

    private void showPDF() {
        if (localPdfFile.exists() && localPdfFile.getTotalSpace() > 10L) {
            showPdfUsing(localPdfFile);
        } else {
            DownloadTask downloadTask = new DownloadTask(PdfViewerActivity.this);
            downloadTask.execute(remotePdfFile);
        }
    }


    //	reload on resume
    @Override
    protected void onResume() {
        super.onResume();
        showPDF();
    }

    //	clear cache to ensure we have good reload
    @Override
    protected void onPause() {
        super.onPause();
        clearAppCash();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation != oldOrientation)
            showPDF();

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //Возвращаем сообщение об ошибке, если что-то пошло не так
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                //скачиваем файл
                input = connection.getInputStream();

                output = new FileOutputStream(localPdfFile);

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

            runOnUiThread(()->showPdfUsing(localPdfFile));
            return null;
        }
    }


    private void showPdfUsing(File localPdfFile) {
        Log.i(TAG, "+++++++Файл " + localPdfFile);
        if (localPdfFile.canRead()) {

            pdfView.fromFile(localPdfFile)
                    .defaultPage(0)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .fitEachPage(true)
                    .onLoad(
                            nbPages ->
                                    Toast.makeText(PdfViewerActivity.this, "Страниц: " + nbPages,
                                            Toast.LENGTH_LONG).show())
                    .defaultPage(0)
                    .load();
        }


    }
}