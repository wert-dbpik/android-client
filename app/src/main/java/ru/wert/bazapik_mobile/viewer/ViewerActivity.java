package ru.wert.bazapik_mobile.viewer;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.concurrent.ExecutionException;

import androidx.fragment.app.FragmentContainerView;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.warnings.Warning1;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.DRAFT_QUICK_SERVICE;
import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;
import static ru.wert.bazapik_mobile.constants.StaticMethods.clearAppCash;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

/**
 * Активность запускается из класса ItemRecViewAdapter
 * принимает ArrayList<String>, состоящий из id чертежей
 */
public class ViewerActivity extends BaseActivity {
    private static final String TAG = "+++ ViewerActivity +++";

    //Формируем путь типа "http://192.168.1.84:8080/drafts/download/drafts/"
    private final String dbdir = DATA_BASE_URL + "drafts/download/drafts/";
    private String remoteFileString, localFileString;
    private Long draftId;
    private Draft currentDraft;
    private int oldOrientation;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        fm = getSupportFragmentManager();
        //Из интента получаем id чертежа
        draftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));

        if (savedInstanceState == null) {
            showProgressIndicator();
        }
        //Этот поток позволяет показать ProgressIndicator
        new Thread(()->{
            //Достаем запись чертежа из БД
            currentDraft = DRAFT_QUICK_SERVICE.findById(draftId);
            if (currentDraft == null) return;
            //Формируем конечный путь до удаленного файла
            remoteFileString = dbdir + draftId + "." + currentDraft.getExtension();
            //Формируем локальный до файла временного хранения
            localFileString = TEMP_DIR + "/" + draftId + "." + currentDraft.getExtension();

            //Проверяем файл в кэше
            File localDraftFile = new File(localFileString);
            if (localDraftFile.exists() && localDraftFile.getTotalSpace() > 10L) {
                Log.d(TAG, String.format("File '%s' was found in cash directory", currentDraft.toUsefulString()));
                runOnUiThread(this::showDraftInViewer);
            //Если файла в кэше нет
            } else {
                try {
                    //Запускаем асинхронную задачу по загрузке файла чертежа
                    String res = new DownloadDraftTask().execute(remoteFileString, localFileString).get();
                    if (res.equals("OK")) {
                        Log.d(TAG, String.format("File '%s' was downloaded with OK message", currentDraft.toUsefulString()));
                        runOnUiThread(this::showDraftInViewer);
                    } else {
                        Log.e(TAG, String.format("remoteFileString = '%s', localFileString = '%s', message from server: %s",
                                remoteFileString, localFileString, res));
                        runOnUiThread(()->{
                            new Warning1().show(ViewerActivity.this, "Внимание!",
                                    "Не удалось загрузить файл чертежа, возможно, сервер не доступен.");
                        });
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "could not download file from server by error: " + e.toString());
                    runOnUiThread(()->{
                        new Warning1().show(ViewerActivity.this, "Внимание!",
                                "Не удалось загрузить файл чертежа, возможно, сервер не доступен.");
                    });
                }
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgressIndicator();
        runOnUiThread(this::showDraftInViewer);

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
        showProgressIndicator();
        if (newConfig.orientation != oldOrientation)
            runOnUiThread(this::showDraftInViewer);
    }

    private void showProgressIndicator() {
        Fragment progressIndicatorFragment = new ProgressIndicatorFragment();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.draft_fragment_container, progressIndicatorFragment);
        ft.commit();
    }

    private void showDraftInViewer() {
        Log.i(TAG, "Current file: " + localFileString);
        File localFile = new File(localFileString);

        Bundle bundle = new Bundle();
        bundle.putString("LOCAL_FILE", localFileString);

        if(localFile.canRead()) {
            //Определяем формат чертежа
            if (currentDraft.getExtension().equals("pdf")) { //Если PDF
                //Переключаем фрагмент на PdfViewer
                Fragment pdfViewerFrag = new PdfViewer();
                pdfViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.draft_fragment_container, pdfViewerFrag);
                ft.commit();

            } else { //Если все остальное
                //Переключаем фрагмент на ImageView
                Fragment imageViewerFrag = new ImageViewer();
                imageViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.draft_fragment_container, imageViewerFrag);
                ft.commit();

            }
        }

    }






}