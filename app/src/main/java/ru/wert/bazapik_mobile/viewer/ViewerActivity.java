package ru.wert.bazapik_mobile.viewer;

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
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.Getter;
import retrofit2.Call;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.organizer.AppOnSwipeTouchListener;
import ru.wert.bazapik_mobile.utils.AnimationDest;
import ru.wert.bazapik_mobile.warnings.AppWarnings;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static android.content.Intent.ACTION_VIEW;
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
    private ArrayList<Draft> allDrafts = new ArrayList<>(); //Лист с id чертежей в PassportInfoActivity
    private Integer iterator; //Текущая позиция
    @Getter private Long currentPassportId;
    @Getter private Draft currentDraft;
    private Passport currentPassport; //инициализируется поздно
    private FragmentManager fm;

    private ImageButton btnShowPrevious, btnShowNext;
    private AnimationDest destination = AnimationDest.ANIMATE_NEXT;
    private Fragment draftFragment;
    private Button btnTapLeft, btnTapRight;
    private ImageButton btnShowRemarks;

    private FragmentContainerView allRemarksContainer;

    public static final String $CURRENT_DRAFT = "current_draft";
    public static final String $CURRENT_PASSPORT = "current_passport";
    public static final String $ALL_DRAFTS = "all_drafts";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);


        //Из интента получаем id чертежа
        currentDraft = getIntent().getParcelableExtra($CURRENT_DRAFT);

        currentPassport = getIntent().getParcelableExtra($CURRENT_PASSPORT);
        currentPassportId = currentPassport.getId();

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
                currentDraft = (allDrafts.get(--iterator));
                destination = AnimationDest.ANIMATE_PREV;
                openFragment();
            }
            public void onSwipeLeft() {
                if(iterator + 1 > allDrafts.size()-1) return;
                currentDraft = allDrafts.get(++iterator);
                destination = AnimationDest.ANIMATE_NEXT;
                openFragment();
            }
        };
    }

    private View.OnClickListener showNextDraft() {
        return v -> {
            currentDraft = allDrafts.get(++iterator);
            destination = AnimationDest.ANIMATE_NEXT;
            openFragment();
        };
    }

    private View.OnClickListener showPreviousDraft() {
        return v -> {
            currentDraft = (allDrafts.get(--iterator));
            destination = AnimationDest.ANIMATE_PREV;
            openFragment();
        };
    }


    private Integer findInitPosition() {
        allDrafts = getIntent().getParcelableArrayListExtra($ALL_DRAFTS);
        for(int iterator = 0; iterator < allDrafts.size(); iterator++){
            if (allDrafts.get(iterator).getId().equals(currentDraft.getId()))
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

        if(iterator.equals(allDrafts.size()-1))
            switchOffButton(btnShowNext);
        else
            switchOnButton(btnShowNext);

//        showProgressIndicator();
        //Этот поток позволяет показать ProgressIndicator
        new Thread(()->{

            List<Remark> remarks;
            RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
            Call<List<Remark>> findRemarksByPassId = api.getAllByPassportId(currentPassport.getId());
            try {
                remarks = findRemarksByPassId.execute().body();
                if (remarks != null && remarks.isEmpty()) {
                    btnShowRemarks.setVisibility(View.INVISIBLE);
                    btnShowRemarks.setClickable(false);
                }
            } catch (IOException e) {
                AppWarnings.showAlert_NoConnection(ViewerActivity.this);
                e.printStackTrace();
            }

            createLog(true, String.format("Открыл чертеж '%s' из комплекта '%s'",
                    currentDraft.toUsefulString(), currentDraft.getFolder().toUsefulString()));

            if (currentDraft == null) return;
            //Формируем конечный путь до удаленного файла
            remoteFileString = dbdir + currentDraft.getId() + "." + currentDraft.getExtension();
            //Формируем локальный до файла временного хранения
            localFileString = TEMP_DIR + "/" + currentDraft.getId() + "." + currentDraft.getExtension();

            //Проверяем файл в кэше
            fileOnScreen = new File(localFileString);
            if (fileOnScreen.exists() && fileOnScreen.getTotalSpace() > 10L) {
                Log.d(TAG, String.format("File '%s' was found in cash directory", currentDraft.toUsefulString()));
                runOnUiThread(this::showDraftInViewer);
                //Если файла в кэше нет
            } else {
                try {
                    //Запускаем асинхронную задачу по загрузке файла чертежа
                    String res = new DownloadFileTask().execute(remoteFileString, localFileString).get();
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

    public void showStatusWarningIfNeeded(TextView warning){
        EDraftStatus status = EDraftStatus.getStatusById(currentDraft.getStatus());
        switch(status){
            case CHANGED:
                warning.setText("ЗАМЕНЕН");
                warning.setVisibility(View.VISIBLE);
                break;
            case ANNULLED:
                warning.setText("АННУЛИРОВАН");
                warning.setVisibility(View.VISIBLE);
            default:
                warning.setText("");
                warning.setVisibility(View.INVISIBLE);
        }
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
                draftFragment = new PdfViewerFragment();
                draftFragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (destination.equals(AnimationDest.ANIMATE_NEXT))
                    ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
                else
                    ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
                ft.replace(R.id.draft_fragment_container, draftFragment);
                ft.commit();

            } else { //Если PNG, JPG, а также показываемое в стороннем приложении
                //Переключаем фрагмент на ImageView
                Fragment draftFragment = new ImageViewerFragment();
                draftFragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                if (destination.equals(AnimationDest.ANIMATE_NEXT))
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
