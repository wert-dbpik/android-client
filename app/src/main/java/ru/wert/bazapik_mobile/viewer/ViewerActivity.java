package ru.wert.bazapik_mobile.viewer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.Getter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.AppOnSwipeTouchListener;
import ru.wert.bazapik_mobile.utils.Dest;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static android.content.Intent.ACTION_VIEW;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_DRAFTS;
import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;
import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.IMAGE_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.PDF_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;
import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;

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
    @Getter private Long currentPassportId;
    @Getter private Draft currentDraft;
    private Passport currentPassport; //инициализируется поздно
    private FragmentManager fm;

    private ImageButton btnShowPrevious, btnShowNext;
    private Dest destination = Dest.NEXT;
    private Fragment draftFragment;
    private Button btnTapLeft, btnTapRight;
    private ImageButton btnShowRemarks;

    private FragmentContainerView allRemarksContainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        //Из интента получаем id чертежа
        currentDraftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));
        currentPassportId = Long.parseLong(getIntent().getStringExtra("PASSPORT_ID"));


        //Инициализируем список чертежей и итератор с текущей позицей
        iterator = findInitPosition();

        Button btnShowInfo = findViewById(R.id.btnShowMenu);
        btnShowInfo.setOnClickListener(v->{
            registerForContextMenu(btnShowInfo);
            this.openContextMenu(btnShowInfo);
            unregisterForContextMenu(btnShowInfo);
        });

        btnShowPrevious = findViewById(R.id.btnShowPrevious);
        btnShowPrevious.setOnClickListener(showPreviousDraft());

        btnShowNext = findViewById(R.id.btnShowNext);
        btnShowNext.setOnClickListener(showNextDraft());
        btnTapLeft = findViewById(R.id.btnTapLeft);
        btnTapLeft.setOnTouchListener(createOnSwipeTouchListener());
        btnTapRight = findViewById(R.id.btnTapRight);
        btnTapRight.setOnTouchListener(createOnSwipeTouchListener());

        btnShowRemarks = findViewById(R.id.btnShowRemarks);
        allRemarksContainer = findViewById(R.id.allRemarksContainer);
        allRemarksContainer.setVisibility(View.INVISIBLE);
        btnShowRemarks.setOnClickListener(view -> {
            allRemarksContainer.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void onBackPressed() {
        if(allRemarksContainer.getVisibility() == View.VISIBLE) {
            allRemarksContainer.setVisibility(View.INVISIBLE);
        } else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fm = getSupportFragmentManager();


    }

    public AppOnSwipeTouchListener createOnSwipeTouchListener(){
        return new AppOnSwipeTouchListener(ViewerActivity.this){
            public void onSwipeRight() {
                if(iterator - 1 < 0) return;
                currentDraftId = (allDraftsIds.get(--iterator));
                destination = Dest.PREV;
                openFragment();
            }
            public void onSwipeLeft() {
                if(iterator + 1 > allDraftsIds.size()-1) return;
                currentDraftId = allDraftsIds.get(++iterator);
                destination = Dest.NEXT;
                openFragment();
            }
        };
    }

    private View.OnClickListener showNextDraft() {
        return v -> {
            currentDraftId = allDraftsIds.get(++iterator);
            destination = Dest.NEXT;
            openFragment();
        };
    }

    private View.OnClickListener showPreviousDraft() {
        return v -> {
            currentDraftId = (allDraftsIds.get(--iterator));
            destination = Dest.PREV;
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
        viewerMenu.inflate(R.menu.viewer_context_menu, menu);
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

    private void switchOnButton(ImageButton btn){
        btn.setVisibility(View.VISIBLE);
        btn.setClickable(true);
    }

    private void switchOffButton(ImageButton btn){
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
            for (Draft d : ALL_DRAFTS) {
                if (d.getId().equals(currentDraftId)) {
                    currentDraft = d;
                    Long passportId = currentDraft.getPassport().getId();
                    for(Passport p: ALL_PASSPORTS){
                        if (p.getId().equals(passportId)) {
                            currentPassport = p;
                            if(currentPassport.getRemarkIds().isEmpty()) {
                                btnShowRemarks.setVisibility(View.INVISIBLE);
                                btnShowRemarks.setClickable(false);
                            }
                        }
                    }

                    createLog(true, String.format("Открыл чертеж '%s' из комплекта '%s'", d.toUsefulString(), d.getFolder().toUsefulString()));
                    break;
                }
            }
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
                            new AlertDialog.Builder(this)
                                    .setTitle("Внимание!")
                                    .setMessage("Не удалось загрузить файл чертежа, возможно, сервер не доступен.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            ViewerActivity.this.finish(); //Закроет активити
                                        }
                                    }).create().show();
                        });
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "could not download file from server by error: " + e.toString());
                    runOnUiThread(()->{
                        new AlertDialog.Builder(this)
                                .setTitle("Внимание!")
                                .setMessage("Не удалось загрузить файл чертежа, возможно, сервер не доступен.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        ViewerActivity.this.finish(); //Закроет активити
                                    }
                                }).create().show();
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
                draftFragment = new PdfViewer();
                draftFragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (destination.equals(Dest.NEXT))
                    ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                else
                    ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.draft_fragment_container, draftFragment);
                ft.commit();

            } else { //Если PNG, JPG, а также показываемое в стороннем приложении
                //Переключаем фрагмент на ImageView
                Fragment draftFragment = new ImageViewer();
                draftFragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (destination.equals(Dest.NEXT))
                    ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                else
                    ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.draft_fragment_container, draftFragment);
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
