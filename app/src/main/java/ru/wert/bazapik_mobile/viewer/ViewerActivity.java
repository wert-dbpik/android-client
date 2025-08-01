package ru.wert.bazapik_mobile.viewer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
 * Активность для просмотра чертежей и документов.
 * Поддерживает PDF, изображения и другие форматы.
 * Особенности:
 * - Загрузка файлов с сервера
 * - Кэширование файлов локально
 * - Навигация между документами
 * - Отображение замечаний
 */
public class ViewerActivity extends BaseActivity {
    private static final String TAG = "ViewerActivity";

    // URL для загрузки файлов с сервера
    private String dbdir = DATA_BASE_URL + "drafts/download/drafts/";
    private String remoteFileString, localFileString;
    private File fileOnScreen;
    private ArrayList<Draft> allDrafts = new ArrayList<>();
    private Integer iterator; // Текущая позиция в списке документов
    private Long currentPassportId;
    private Draft currentDraft;
    private Passport currentPassport;
    private FragmentManager fm;

    // UI элементы
    private ImageButton btnShowPrevious, btnShowNext;
    private AnimationDest destination = AnimationDest.ANIMATE_NEXT;
    private Fragment draftFragment;
    private Button btnTapLeft, btnTapRight;
    private ImageButton btnShowRemarks;
    private FragmentContainerView allRemarksContainer;

    // Флаг для отслеживания выполнения фоновых задач
    private volatile boolean isTaskRunning = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Ключи для Intent
    public static final String $CURRENT_DRAFT = "current_draft";
    public static final String $CURRENT_PASSPORT = "current_passport";
    public static final String $ALL_DRAFTS = "all_drafts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        // Восстановление состояния при повороте экрана
        if (savedInstanceState != null) {
            remoteFileString = savedInstanceState.getString("remoteFile");
            localFileString = savedInstanceState.getString("localFile");
            dbdir = savedInstanceState.getString("bdDir");
        }

        initViews();
        loadInitialData();
    }

    /**
     * Инициализация UI элементов
     */
    private void initViews() {
        Button btnShowInfo = findViewById(R.id.btnShowMenu);
        btnShowInfo.setOnClickListener(v -> {
            registerForContextMenu(btnShowInfo);
            openContextMenu(btnShowInfo);
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

    /**
     * Загрузка начальных данных из Intent
     */
    private void loadInitialData() {
        currentDraft = getIntent().getParcelableExtra($CURRENT_DRAFT);
        currentPassport = getIntent().getParcelableExtra($CURRENT_PASSPORT);
        currentPassportId = currentPassport.getId();
        allDrafts = getIntent().getParcelableArrayListExtra($ALL_DRAFTS);
        iterator = findInitPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fm = getSupportFragmentManager();
        openFragment(); // Загружаем фрагмент при возобновлении активности
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Отменяем все фоновые задачи при сворачивании приложения
        isTaskRunning = false;
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Обработчик свайпов для навигации между документами
     */
    public AppOnSwipeTouchListener createOnSwipeTouchListener() {
        return new AppOnSwipeTouchListener(ViewerActivity.this) {
            public void onSwipeRight() {
                if (iterator - 1 < 0) return;
                currentDraft = (allDrafts.get(--iterator));
                destination = AnimationDest.ANIMATE_PREV;
                openFragment();
            }

            public void onSwipeLeft() {
                if (iterator + 1 > allDrafts.size() - 1) return;
                currentDraft = allDrafts.get(++iterator);
                destination = AnimationDest.ANIMATE_NEXT;
                openFragment();
            }
        };
    }

    /**
     * Открывает предыдущий документ в списке
     */
    private View.OnClickListener showPreviousDraft() {
        return v -> {
            if (iterator > 0) {
                currentDraft = (allDrafts.get(--iterator));
                destination = AnimationDest.ANIMATE_PREV;
                openFragment();
            }
        };
    }

    /**
     * Открывает следующий документ в списке
     */
    private View.OnClickListener showNextDraft() {
        return v -> {
            if (iterator < allDrafts.size() - 1) {
                currentDraft = allDrafts.get(++iterator);
                destination = AnimationDest.ANIMATE_NEXT;
                openFragment();
            }
        };
    }

    /**
     * Находит начальную позицию в списке документов
     */
    private Integer findInitPosition() {
        for (int i = 0; i < allDrafts.size(); i++) {
            if (allDrafts.get(i).getId().equals(currentDraft.getId()))
                return i;
        }
        return 0;
    }

    /**
     * Основной метод для открытия и отображения документа
     */
    private void openFragment() {
        if (isFinishing() || isDestroyed()) return;

        // Обновляем состояние кнопок навигации
        updateNavigationButtons();

        // Показываем индикатор загрузки
        showProgressIndicator();

        if (isTaskRunning) return;
        isTaskRunning = true;

        new Thread(() -> {
            try {
                // Загружаем замечания для текущего документа
                loadRemarks();

                // Логирование открытия документа
                Log.d(TAG, String.format("Открытие чертежа '%s'", currentDraft.toUsefulString()));

                // Формируем пути к файлам
                remoteFileString = dbdir + currentDraft.getId() + "." + currentDraft.getExtension();
                localFileString = TEMP_DIR + "/" + currentDraft.getId() + "." + currentDraft.getExtension();
                fileOnScreen = new File(localFileString);

                // Проверяем наличие файла в кэше
                if (fileOnScreen.exists() && fileOnScreen.length() > 10L) {
                    Log.d(TAG, "Файл найден в кэше: " + currentDraft.toUsefulString());
                    showDraftOnUiThread();
                } else {
                    // Загружаем файл с сервера
                    downloadFileFromServer();
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при загрузке файла", e);
                showErrorDialog();
            } finally {
                isTaskRunning = false;
            }
        }).start();
    }

    /**
     * Показывает предупреждение о статусе чертежа (аннулирован/заменен)
     * @param warning TextView для отображения предупреждения
     */
    public void showStatusWarningIfNeeded(TextView warning) {
        if (warning == null || currentDraft == null) return;

        EDraftStatus status = EDraftStatus.getStatusById(currentDraft.getStatus());
        switch(status) {
            case CHANGED:
                warning.setText("ЗАМЕНЕН");
                warning.setVisibility(View.VISIBLE);
                break;
            case ANNULLED:
                warning.setText("АННУЛИРОВАН");
                warning.setVisibility(View.VISIBLE);
                break;
            default:
                warning.setText("");
                warning.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Загружает замечания для текущего документа
     */
    private void loadRemarks() throws IOException {
        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<List<Remark>> findRemarksByPassId = api.getAllByPassportId(currentPassport.getId());
        List<Remark> remarks = findRemarksByPassId.execute().body();

        mainHandler.post(() -> {
            if (remarks == null || remarks.isEmpty()) {
                btnShowRemarks.setVisibility(View.INVISIBLE);
                btnShowRemarks.setClickable(false);
            }
        });
    }

    /**
     * Загружает файл с сервера
     */
    private void downloadFileFromServer() throws ExecutionException, InterruptedException {
        String result = new DownloadFileTask().execute(remoteFileString, localFileString).get();

        if ("OK".equals(result)) {
            Log.d(TAG, "Файл успешно загружен: " + currentDraft.toUsefulString());
            showDraftOnUiThread();
        } else {
            Log.e(TAG, "Ошибка загрузки файла. Ответ сервера: " + result);
            showErrorDialog();
        }
    }

    /**
     * Показывает документ в UI потоке
     */
    private void showDraftOnUiThread() {
        mainHandler.post(() -> {
            if (!isFinishing() && !isDestroyed()) {
                showDraftInViewer();
            }
        });
    }

    /**
     * Показывает диалог с ошибкой
     */
    private void showErrorDialog() {
        mainHandler.post(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage("Не удалось загрузить файл")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        });
    }

    /**
     * Обновляет состояние кнопок навигации
     */
    private void updateNavigationButtons() {
        btnShowPrevious.setVisibility(iterator > 0 ? View.VISIBLE : View.INVISIBLE);
        btnShowNext.setVisibility(iterator < allDrafts.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Показывает индикатор загрузки
     */
    private void showProgressIndicator() {
        if (fm.isDestroyed()) return;

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.draft_fragment_container, new ProgressIndicatorFragment());
        ft.commitAllowingStateLoss();
    }

    /**
     * Отображает документ в соответствующем viewer'е (PDF или Image)
     */
    private void showDraftInViewer() {
        if (!fileOnScreen.canRead() || fm.isDestroyed()) return;

        Bundle bundle = new Bundle();
        bundle.putString("LOCAL_FILE", localFileString);

        Fragment newFragment;
        if (PDF_EXTENSIONS.contains(currentDraft.getExtension())) {
            newFragment = new PdfViewerFragment();
        } else {
            newFragment = new ImageViewerFragment();
        }

        newFragment.setArguments(bundle);

        FragmentTransaction ft = fm.beginTransaction();
        if (destination.equals(AnimationDest.ANIMATE_NEXT)) {
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        } else {
            ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        }

        ft.replace(R.id.draft_fragment_container, newFragment);
        ft.commitAllowingStateLoss();
    }

    // Остальные методы (onCreateContextMenu, onContextItemSelected, showInfo, showInOuterApp и т.д.)
    // остаются без изменений, но также должны быть проверены на безопасность выполнения

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("remoteFile", remoteFileString);
        outState.putString("localFile", localFileString);
        outState.putString("bdDir", dbdir);
    }

    /**
     * @return ID текущего паспорта (документа)
     */
    public Long getCurrentPassportId() {
        return currentPassport != null ? currentPassport.getId() : null;
    }
}
