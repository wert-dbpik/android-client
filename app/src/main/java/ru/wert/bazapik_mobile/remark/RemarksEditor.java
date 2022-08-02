package ru.wert.bazapik_mobile.remark;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.FileRetrofitService;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.PicRetrofitService;
import ru.wert.bazapik_mobile.pics.PicsAdapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
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

import static ru.wert.bazapik_mobile.info.InfoActivity.REMARK_PASSPORT;

public class RemarksEditor extends AppCompatActivity implements
        FileRetrofitService.IFileUploader, PicRetrofitService.IPicCreator{

    private final String TAG = "RemarkFragment";

    private Passport passport;
    private String text;
    private List<Pic> pics;
    private RecyclerView rvEditorRemarkPics;
    private List<Pic> picsInAdapter;
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

    private Bundle createResumeBundle(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(REMARK_PASSPORT, passport);
        bundle.putString(REMARK_TEXT, text);
        bundle.putParcelableArrayList(REMARK_PICS, (ArrayList<? extends Parcelable>) pics);

        Parcelable listState = Objects.requireNonNull(rvEditorRemarkPics.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }

    private void deployResumeBundle(){
        this.passport = resumeBundle.getParcelable(REMARK_PASSPORT);
        this.text = resumeBundle.getString(REMARK_TEXT);
        this.pics = resumeBundle.getParcelableArrayList(REMARK_PICS);
        resumeBundle = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remarks_editor);

        Intent intent = getIntent();
        passport = intent.getParcelableExtra(REMARK_PASSPORT);

        editText = findViewById(R.id.etTextRemark);
        tvTitle = findViewById(R.id.tvRemarkTitle);
        btnAdd = findViewById(R.id.btnAddRemark);
        btnAdd.setOnClickListener(v->{
            if(btnAdd.getText().equals(sAdd))
                addRemark();
            else
                changeRemark();
        });

        pickUpPictureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null) {
                            List<Uri> chosenPics;
                            ClipData clipData = data.getClipData();
                            chosenPics = (clipData == null ?
                                    Collections.singletonList(data.getData()) :
                                    ThisApplication.clipDataToList(clipData));

                            for(Uri uri : chosenPics){
                                String ext;
                                String mimeType = getContentResolver().getType(uri);
                                if(mimeType.startsWith("image")) {
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
        btnAddImage.setOnClickListener(v ->{
            Intent addImageIntent = new Intent();
            addImageIntent.setAction(Intent.ACTION_GET_CONTENT);
            addImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            addImageIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            pickUpPictureResultLauncher.launch(addImageIntent);
        });

        rvEditorRemarkPics = findViewById(R.id.rvEditorRemarkPics);
        picsInAdapter = new ArrayList<>();
        fillRecViewWithPics(picsInAdapter);


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
        picsAdapter = new PicsAdapter(this, pics, PicsAdapter.EDITOR_FRAGMENT);
        rvEditorRemarkPics.setAdapter(picsAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void addRemark(){
//        activity.getRm().addRemark(picsInAdapter);
    }


    private void changeRemark(){

//        activity.getRm().changeRemark(picsInAdapter);
    }

    /**
     * Метод удаляет из редактора текст и изображения
     */
    public void clearRemarkEditor() {
        picsInAdapter = new ArrayList<>();
        picsAdapter.changeListOfItems(picsInAdapter);
        editText.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(resumeBundle != null) {
            deployResumeBundle();
            runOnUiThread(()->{
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