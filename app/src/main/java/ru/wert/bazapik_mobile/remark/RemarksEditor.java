package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Setter;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.FileRetrofitService;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.PicRetrofitService;
import ru.wert.bazapik_mobile.pics.PicsAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.info.InfoActivity.ADD_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.CHANGING_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.NEW_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.REMARK_PASSPORT;
import static ru.wert.bazapik_mobile.info.InfoActivity.TYPE_OF_REMARK_OPERATION;

public class RemarksEditor extends AppCompatActivity implements
        FileRetrofitService.IFileUploader, PicRetrofitService.IPicCreator {

    private final String TAG = "RemarkFragment";

    private Passport passport;
    private String text;
    private List<Pic> pics;
    private RecyclerView rvEditorRemarkPics;
    @Setter private List<Pic> picsInAdapter;
    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";
    private PicsAdapter picsAdapter;

    private EditText editText;
    private TextView tvTitle;
    private Button btnAdd;

    private Bundle resumeBundle;
    private final String REMARK_TEXT = "remark_text";
    private final String REMARK_PICS = "remark_pics";
    private final String KEY_RECYCLER_STATE = "recycler_state";

    private int typeOfRemarkOperation;
    private Remark changingRemark;

    private Bundle createResumeBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(REMARK_PASSPORT, passport);
        bundle.putString(REMARK_TEXT, text);
        bundle.putParcelableArrayList(REMARK_PICS, (ArrayList<? extends Parcelable>) pics);

        Parcelable listState = Objects.requireNonNull(rvEditorRemarkPics.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }

    private void deployResumeBundle() {
        this.passport = resumeBundle.getParcelable(REMARK_PASSPORT);
        this.text = resumeBundle.getString(REMARK_TEXT);
        this.pics = resumeBundle.getParcelableArrayList(REMARK_PICS);
        resumeBundle = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remarks_editor);

        editText = findViewById(R.id.etTextRemark);
        tvTitle = findViewById(R.id.tvRemarkTitle);
        btnAdd = findViewById(R.id.btnAddRemark);
        rvEditorRemarkPics = findViewById(R.id.rvEditorRemarkPics);


        Intent intent = getIntent();
        typeOfRemarkOperation = intent.getIntExtra(TYPE_OF_REMARK_OPERATION, 1);
        passport = intent.getParcelableExtra(REMARK_PASSPORT);

        if (typeOfRemarkOperation == ADD_REMARK) {
            tvTitle.setText("Добавление комментария");
            btnAdd.setText(sAdd);
            picsInAdapter = new ArrayList<>();
            fillRecViewWithPics(picsInAdapter);
        } else { //CHANGE_REMARK
            changingRemark = intent.getParcelableExtra(CHANGING_REMARK);
            tvTitle.setText("Изменение комментария");
            btnAdd.setText(sChange);
            editText.setText(changingRemark.getText());
            picsInAdapter = changingRemark.getPicsInRemark();
            fillRecViewWithPics(picsInAdapter);
        }

        btnAdd.setOnClickListener(v -> {
            if (btnAdd.getText().equals(sAdd))
                addRemark();
            else
                changeRemark();
        });

        pickUpPictureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            List<Uri> chosenPics;
                            ClipData clipData = data.getClipData();
                            chosenPics = (clipData == null ?
                                    Collections.singletonList(data.getData()) :
                                    ThisApplication.clipDataToList(clipData));

                            for (Uri uri : chosenPics) {
                                String ext;
                                String mimeType = getContentResolver().getType(uri);
                                if (mimeType.startsWith("image")) {
                                    String str = mimeType.split("/", -1)[1];
                                    ext = str.equals("jpeg") ? "jpg" : str;
                                } else
                                    return;
                                PicRetrofitService.create(RemarksEditor.this, this, uri, ext);
                                //Смотри doWhenPicIsCreated

                            }
                        }
                    }
                });

        ImageButton btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(v -> {
            Intent addImageIntent = new Intent();
            addImageIntent.setAction(Intent.ACTION_GET_CONTENT);
            addImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            addImageIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            pickUpPictureResultLauncher.launch(addImageIntent);
        });

    }

    @Override//IPicCreator
    public void doWhenPicHasBeenCreated(Response<Pic> response, Uri uri) {
        //Добавляем выбранную картинку в коллекцию для адаптера
        picsInAdapter.add(response.body());
        try {
            Pic savedPic = response.body();
            String fileNewName = savedPic.getId() + "." + "jpg";

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] draftBytes = baos.toByteArray();

            FileRetrofitService.uploadFile(RemarksEditor.this, this, "pics", fileNewName, draftBytes);
            //doWhenFileHasBeenUploaded
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override //FileRetrofitService.IFileUploader
    public void doWhenFileHasBeenUploaded() {

        picsAdapter.changeListOfItems(new ArrayList<>(picsInAdapter));

//        viewInteraction.updateRemarkAdapter();
    }

    private void fillRecViewWithPics(List<Pic> pics) {
        rvEditorRemarkPics.setLayoutManager(new LinearLayoutManager(this));
        picsAdapter = new PicsAdapter(this, pics, PicsAdapter.REMARK_EDITOR);
        rvEditorRemarkPics.setAdapter(picsAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void addRemark() {
        Remark remark = new Remark(
                passport,
                CURRENT_USER,
                editText.getText().toString(),
                ThisApplication.getCurrentTime(),
                picsInAdapter
        );

        Intent data = new Intent();
        data.putExtra(NEW_REMARK, remark);
        setResult(RESULT_OK, data);
        finish();
    }


    private void changeRemark() {
        changingRemark.setUser(CURRENT_USER);
        changingRemark.setText(editText.getText().toString());
        changingRemark.setCreationTime(ThisApplication.getCurrentTime());
        changingRemark.setPicsInRemark(picsInAdapter);

        Intent data = new Intent();
        data.putExtra(CHANGING_REMARK, changingRemark);
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (resumeBundle != null) {
            deployResumeBundle();
            runOnUiThread(() -> {
                editText.setText(resumeBundle.getString(REMARK_TEXT));
                editText.setSelection(editText.length());

                Parcelable savedRecyclerLayoutState = resumeBundle.getParcelable(KEY_RECYCLER_STATE);
                Objects.requireNonNull(rvEditorRemarkPics.getLayoutManager()).onRestoreInstanceState(savedRecyclerLayoutState);
            });

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        createResumeBundle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        createResumeBundle();
    }
}