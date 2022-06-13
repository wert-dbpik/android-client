package ru.wert.bazapik_mobile.viewer;

import static android.content.Intent.ACTION_VIEW;
import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.DRAFT_QUICK_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.IMAGE_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.PDF_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;
import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.utils.Direction;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

/**
 * Активность запускается из класса ItemRecViewAdapter
 * принимает ArrayList<String>, состоящий из id чертежей
 */
public class ViewerActivity extends BaseActivity {
    private static final String TAG = "+++ ViewerActivity +++";

    //Формируем путь типа "http://192.168.1.84:8080/drafts/download/drafts/"
    private String dbdir = DATA_BASE_URL + "drafts/download/drafts/";
    private String remoteFileString, localFileString;
    private File fileOnScreen;
    private ArrayList<Long> allDraftsIds = new ArrayList<>(); //Лист с id чертежей в PassportInfoActivity
    private Integer iterator; //Текущая позиция
    private Long currentDraftId; //id текущего чертежа на экране
    private Draft currentDraft;
    private FragmentManager fm;

    private Button btnShowPrevious, btnShowNext;
    private Direction direction = Direction.NEXT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        fm = getSupportFragmentManager();
        //Из интента получаем id чертежа
        currentDraftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));
        //Инициализируем список чертежей и итератор с текущей позицей
        iterator = findInitPosition();

        Button btnShowInfo = findViewById(R.id.btnShowMenu);
        btnShowInfo.setOnClickListener(V->{
            registerForContextMenu(btnShowInfo);
            this.openContextMenu(btnShowInfo);
            unregisterForContextMenu(btnShowInfo);
        });

        btnShowPrevious = findViewById(R.id.btnShowPrevious);
        btnShowPrevious.setOnClickListener(showPreviousDraft());

        btnShowNext = findViewById(R.id.btnShowNext);
        btnShowNext.setOnClickListener(showNextDraft());

    }

    private View.OnClickListener showNextDraft() {
        return v -> {
            currentDraftId = allDraftsIds.get(++iterator);
            direction = Direction.NEXT;
            openFragment();
        };
    }

    private View.OnClickListener showPreviousDraft() {
        return v -> {
            currentDraftId = (allDraftsIds.get(--iterator));
            direction = Direction.PREV;
            openFragment();
        };
    }

    private Integer findInitPosition() {
        ArrayList<String> foundDrafts = (ArrayList<String>) getIntent().getStringArrayListExtra("DRAFTS");
        for (String s : foundDrafts) {
            allDraftsIds.add(Long.parseLong(s));
        }
        for(int iterator = 0; iterator < allDraftsIds.size(); iterator++){
            if (allDraftsIds.get(iterator).equals(currentDraftId))
                return iterator;
        }
        return null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater viewerMenu = getMenuInflater();
        viewerMenu.inflate(R.menu.viewer_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.showInfo:
                showInfo();
                break;
            case R.id.showInApplication:
                showInOuterApp();
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        openFragment();
    }

    private void switchOnButton(Button btn){
        btn.setVisibility(View.VISIBLE);
        btn.setClickable(true);
    }

    private void switchOffButton(Button btn){
        btn.setVisibility(View.INVISIBLE);
        btn.setClickable(false);
    }

    private void openFragment(){
        if (iterator.equals(0))
            switchOffButton(btnShowPrevious);
        else
            switchOnButton(btnShowPrevious);

        if(iterator.equals(allDraftsIds.size()-1))
            switchOffButton(btnShowNext);
        else
            switchOnButton(btnShowNext);

//        showProgressIndicator();
        //Этот поток позволяет показать ProgressIndicator
        new Thread(()->{
            //Достаем запись чертежа из БД
            currentDraft = DRAFT_QUICK_SERVICE.findById(currentDraftId);
            if (currentDraft == null) return;
            //Формируем конечный путь до удаленного файла
            remoteFileString = dbdir + currentDraftId + "." + currentDraft.getExtension();
            //Формируем локальный до файла временного хранения
            localFileString = TEMP_DIR + "/" + currentDraftId + "." + currentDraft.getExtension();

            //Проверяем файл в кэше
            fileOnScreen = new File(localFileString);
            if (fileOnScreen.exists() && fileOnScreen.getTotalSpace() > 10L) {
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
        Bundle bundle = new Bundle();
        bundle.putString("LOCAL_FILE", localFileString);

        if (fileOnScreen.canRead()) {
            //Определяем формат чертежа
            if (PDF_EXTENSIONS.contains(currentDraft.getExtension())) { //Если PDF
                //Переключаем фрагмент на PdfViewer
                Fragment pdfViewerFrag = new PdfViewer();
                pdfViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (direction.equals(Direction.NEXT))
                    ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                else
                    ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.draft_fragment_container, pdfViewerFrag);
                ft.commit();

            } else { //Если PNG, JPG, а также показываемое в стороннем приложении
                //Переключаем фрагмент на ImageView
                Fragment imageViewerFrag = new ImageViewer();
                imageViewerFrag.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (direction.equals(Direction.NEXT))
                    ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                else
                    ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.draft_fragment_container, imageViewerFrag);
                ft.commit();

            }
        }

    }

    private void showInfo() {
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
    }

    public void showInOuterApp() {
        String bundleString = fileOnScreen.toString();
        String ext = FileUtils.getExtension(bundleString);
        String mimeType;
        if (PDF_EXTENSIONS.contains(ext))
            mimeType = "application/pdf";
        else if (IMAGE_EXTENSIONS.contains(ext))
            mimeType = "image/*";
        else if (SOLID_EXTENSIONS.contains(ext))
            mimeType = "application/solidworks-file";
        else return;

        Intent intent = new Intent();
        intent.setAction(ACTION_VIEW);

        Uri contentUri = FileProvider.getUriForFile(this, getApplication().getPackageName() + ".fileprovider", fileOnScreen);
        intent.setDataAndType(contentUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);

    }


}
