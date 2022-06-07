package ru.wert.bazapik_mobile.viewer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static android.content.Intent.ACTION_VIEW;
import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.DRAFT_QUICK_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.IMAGE_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.PDF_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;
import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Активность запускается из класса ItemRecViewAdapter
 * принимает ArrayList<String>, состоящий из id чертежей
 */
public class ViewerActivity extends BaseActivity {
    private static final String TAG = "+++ ViewerActivity +++";

    //Формируем путь типа "http://192.168.1.84:8080/drafts/download/drafts/"
    private String dbdir = DATA_BASE_URL + "drafts/download/drafts/";
    private String remoteFileString, localFileString;
    private Long draftId;
    private Draft currentDraft;
    private int oldOrientation;
    private FragmentManager fm;
    private Button btnGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        btnGo = findViewById(R.id.btnGo);
        fm = getSupportFragmentManager();
        //Из интента получаем id чертежа
        draftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));

        Button btnShowInfo = findViewById(R.id.btnShowInfo);
        btnShowInfo.setOnClickListener(e->{
            String decNumber = currentDraft.getPassport().getNumberWithPrefix();
            String name = currentDraft.getPassport().getName();
            String notes = currentDraft.getNote() == null || currentDraft.getNote().equals("")? "-отсутствует-": currentDraft.getNote();
            String annul = currentDraft.getStatus().equals(EDraftStatus.ANNULLED.getStatusId())?
                    "c " + parseLDTtoDate(currentDraft.getWithdrawalTime())  + "\n" +  currentDraft.getWithdrawalUser().getName() + "\n\n" :
                    "\n";

            new WarningDialog1().show(ViewerActivity.this,
                    decNumber + "\n" + name,
                    "Добавил:  " + parseLDTtoDate(currentDraft.getCreationTime()) + "\n" +
                     currentDraft.getCreationUser().getName() + "\n\n" +
                            "Тип-стр:  " + EDraftType.getDraftTypeById(currentDraft.getDraftType()).getShortName() + "-" + currentDraft.getPageNumber() + "\n"  +
                            "Статус:   " + EDraftStatus.getStatusById(currentDraft.getStatus()).getStatusName() + "\n" + annul +
                            "Источник: \n" + currentDraft.getFolder().toUsefulString() + "\n\n" +
                            "Примечание: \n" + notes


            );
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressIndicator();
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
                            new WarningDialog1().show(ViewerActivity.this, "Внимание!",
                                    "Не удалось загрузить файл чертежа, возможно, сервер не доступен.");
                        });
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "could not download file from server by error: " + e.toString());
                    runOnUiThread(()->{
                        new WarningDialog1().show(ViewerActivity.this, "Внимание!",
                                "Не удалось загрузить файл чертежа, возможно, сервер не доступен.");
                    });
                }
            }
        }).start();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("remoteFile", remoteFileString);
        savedInstanceState.putString("localFile", localFileString);
        savedInstanceState.putString("bdDir", dbdir);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        remoteFileString = savedInstanceState.getString("remoteFile");
        localFileString = savedInstanceState.getString("localFile");
        dbdir = savedInstanceState.getString("bdDir");

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

        if (localFile.canRead()) {
            //Определяем формат чертежа
            if (PDF_EXTENSIONS.contains(currentDraft.getExtension())) { //Если PDF
                //Переключаем фрагмент на PdfViewer
                Fragment pdfViewerFrag = new PdfViewer();
                pdfViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.draft_fragment_container, pdfViewerFrag);
                ft.commit();

            } else { //Если PNG, JPG, а также показываемое в стороннем приложении
                //Переключаем фрагмент на ImageView
                Fragment imageViewerFrag = new ImageViewer();
                imageViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.draft_fragment_container, imageViewerFrag);
                ft.commit();

            }
        }

    }

    public void createButtonGo(String bundleString, String type) {
        btnGo.setOnClickListener(e->{
            Intent intent = new Intent();
            intent.setAction(ACTION_VIEW);
            intent.setDataAndType(Uri.parse(bundleString), type);
            startActivity(intent);
        });
    }

    public void createButtonGo2(String bundleString) {
        btnGo.setOnClickListener(e->{
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setPackage(getPackageManager().getLaunchIntentForPackage("com.solidworks.eDrawingsProAndroid"));
//            PackageManager pm = getPackageManager();
//
//            Intent intent = pm.;
//
//            intent.setAction(ACTION_VIEW);
//
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            startActivity(intent);

//            int id = 0;
//            List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);
//            for(int i = 0; i < apps.size(); i++){
//                if(apps.get(i).packageName.contains("solid")) {
//                    id = i;
//                    break;
//                }
//            }

//            Intent launchApp = getPackageManager().getLaunchIntentForPackage("com.solidworks.eDrawingsProAndroid");
//            startActivity(launchApp);
//
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.solidworks.eDrawingsAndroid","com.solidworks.eDrawingsAndroid.eDrawingsApp"));
            startActivity(intent);

//            String packageName = apps.get(id).packageName;
//            String className = apps.get(id).applicationInfo.className;
//
//            Intent intent = new Intent(ACTION_VIEW);
////            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setPackage(packageName);
////            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setClassName(packageName, className);
//            startActivity(intent);

        });
    }

}