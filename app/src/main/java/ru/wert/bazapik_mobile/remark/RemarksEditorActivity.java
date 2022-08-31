package ru.wert.bazapik_mobile.remark;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.PicApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.FileRetrofitService;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.pics.PicsAdapter;
import ru.wert.bazapik_mobile.pics.PicsUriAdapter;
import ru.wert.bazapik_mobile.warnings.AppWarnings;

import static androidx.core.content.FileProvider.getUriForFile;
import static ru.wert.bazapik_mobile.ThisApplication.REQUEST_CODE_PERMISSION_CAMERA;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.info.InfoActivity.ADD_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.CHANGING_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.NEW_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.REMARK_PASSPORT;
import static ru.wert.bazapik_mobile.info.InfoActivity.TYPE_OF_REMARK_OPERATION;

public class RemarksEditorActivity extends BaseActivity implements
        FileRetrofitService.IFileUploader {

    private final String TAG = "RemarkFragment";

    private Passport passport;
    private String text;
    private List<Pic> pics;
    @Setter private List<Uri> uriInAdapter; //Претенденты на добавление
    private RecyclerView rvEditorRemarkPics;
    @Setter private List<Pic> picsInAdapter;
    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private ActivityResultLauncher<Intent> takePhotoResultLauncher;
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";
    private PicsAdapter picsAdapter;
    private PicsUriAdapter picsUriAdapter;

    private EditText editText;
    private TextView tvTitle;
    private Button btnAdd;
    private ImageButton btnAddPhoto, btnAddImage;
    private LinearLayout llAddPicsButtons;
    private Uri imageUri; //uri сделанной фотографии

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
        llAddPicsButtons = findViewById(R.id.llAddPicsButtons);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddImage = findViewById(R.id.btnAddImage);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // разрешение не предоставлено
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
        }

        Intent intent = getIntent();
        typeOfRemarkOperation = intent.getIntExtra(TYPE_OF_REMARK_OPERATION, 1);
        passport = intent.getParcelableExtra(REMARK_PASSPORT);

        if (typeOfRemarkOperation == ADD_REMARK) {
            tvTitle.setText("Добавление комментария");
            btnAdd.setText(sAdd);
            uriInAdapter = new ArrayList<>();
            fillRecViewWithUris(uriInAdapter);
        } else { //CHANGE_REMARK
            changingRemark = intent.getParcelableExtra(CHANGING_REMARK);
            tvTitle.setText("Изменение комментария");
            btnAdd.setText(sChange);
            editText.setText(changingRemark.getText());
//            uriInAdapter = changingRemark.getPicsInRemark();
            fillRecViewWithUris(uriInAdapter);
        }

        btnAdd.setOnClickListener(v -> {
            if (btnAdd.getText().equals(sAdd)){
                AsyncTask<List<Uri>, Void, Remark> addRemark = new SaveRemarkTask();
                addRemark.execute(uriInAdapter);
            }
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



                            uriInAdapter.addAll(chosenPics);
                            fillRecViewWithUris(uriInAdapter);

                        }
                    }
                });

        takePhotoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        uriInAdapter.add(imageUri);
                        fillRecViewWithUris(uriInAdapter);
//                        PicRetrofitService.create(RemarksEditorActivity.this, this, imageUri, "jpg");
                        //Смотри doWhenPicIsCreated
                    }
                });

        btnAddImage.setOnClickListener(v -> {
            Intent addImageIntent = new Intent();
            addImageIntent.setAction(Intent.ACTION_GET_CONTENT);
            addImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            addImageIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            pickUpPictureResultLauncher.launch(addImageIntent);
        });

        btnAddPhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // разрешение не предоставлено
                showToast("Отсутствует разрешение на использование камеры");
                return;
            }
            Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageUri = FileProvider.getUriForFile(this, getApplication().getPackageName() + ".fileprovider", getFile());
            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if (intent.resolveActivity(getPackageManager()) != null)
                takePhotoResultLauncher.launch(takePhoto);

        });

    }

    private File getFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image_file = null;

        try {
            File outputDir = getCacheDir(); // context being the Activity pointer
            image_file = File.createTempFile(imageFileName, ".jpg", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String mCurrentPhotoPath = image_file.getAbsolutePath();
        return image_file;
    }



    @Override //FileRetrofitService.IFileUploader
    public void doWhenFileHasBeenUploaded() {

        picsAdapter.changeListOfItems(new ArrayList<>(picsInAdapter));

//        viewInteraction.updateRemarkAdapter();
    }

    private void fillRecViewWithPics(List<Pic> pics) {
        rvEditorRemarkPics.setLayoutManager(new LinearLayoutManager(this));
        picsAdapter = new PicsAdapter(this, pics, PicsAdapter.REMARK_EDITOR, this);
        rvEditorRemarkPics.setAdapter(picsAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void fillRecViewWithUris(List<Uri> pics) {
        rvEditorRemarkPics.setLayoutManager(new LinearLayoutManager(this));
        picsUriAdapter = new PicsUriAdapter(this, pics, PicsUriAdapter.REMARK_EDITOR, this);
        rvEditorRemarkPics.setAdapter(picsUriAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

//    private void saveUriInAdapter(){
//        for (Uri uri : uriInAdapter) {
//            String ext;
//            String mimeType = getContentResolver().getType(uri);
//            if (mimeType.startsWith("image")) {
//                String str = mimeType.split("/", -1)[1];
//                ext = str.equals("jpeg") ? "jpg" : str;
//            } else
//                return;
//            Pic pic = PicRetrofitService.create(RemarksEditorActivity.this, this, uri, ext);
//            //Смотри doWhenPicIsCreated
//        }
//    }

//    @Override//IPicCreator
//    public void doWhenPicHasBeenCreated(Response<Pic> response, Uri uri) {
//        try {
//            Pic savedPic = response.body();
//            String fileNewName = savedPic.getId() + "." + "jpg";
//
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//            byte[] draftBytes = baos.toByteArray();
//
//            FileRetrofitService.uploadFile(RemarksEditorActivity.this, this, "pics", fileNewName, draftBytes);
//            //doWhenFileHasBeenUploaded
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void addRemark() {
//        saveUriInAdapter();
//
//        Remark remark = new Remark(
//                passport,
//                CURRENT_USER,
//                editText.getText().toString(),
//                ThisApplication.getCurrentTime(),
//                picsInAdapter
//        );
//
//        Intent data = new Intent();
//        data.putExtra(NEW_REMARK, remark);
//        setResult(RESULT_OK, data);
//        finish();
//    }


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    showToast("Разрешение добавлять фото получено!");
                } else {
                    // permission denied
                    showToast("Вы не сможете добавлять снимки!");
                }
                return;

        }
    }

    private class SaveRemarkTask extends AsyncTask<List<Uri>, Void, Remark> {

        Remark savedRemark;

        @SafeVarargs
        @Override
        protected final Remark doInBackground(List<Uri>... uris) {

            PicApiInterface picApi = RetrofitClient.getInstance().getRetrofit().create(PicApiInterface.class);
            RemarkApiInterface remarkApi = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
            List<Pic> allPics = new ArrayList<>();
            List<Uri> allUris = uris[0];
            for (Uri uri : allUris) {
                Pic newPic = null;
                try {
                    newPic = new Pic();
                    Bitmap bmp = Picasso.get().load(uri).get(); //Здесь может быть ошибка
                    String ext;
                    String mimeType = getContentResolver().getType(uri);
                    if (mimeType.startsWith("image")) {
                        String str = mimeType.split("/", -1)[1];
                        ext = str.equals("jpeg") ? "jpg" : str;
                    } else
                        break; //Если картинка не картинка, то переходим к следующему uri
                    newPic.setExtension(ext);
                    newPic.setWidth(bmp.getWidth());
                    newPic.setHeight(bmp.getHeight());
                    newPic.setUser(CURRENT_USER);
                    newPic.setTime(ThisApplication.getCurrentTime());

                    Call<Pic> call = picApi.create(newPic);
                    Pic pic = call.execute().body();


                    uploadPicToDataBase(uri, pic);

                    allPics.add(pic);
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка декодирования файла: " + e.getMessage());
                }

            }

            Remark remark = new Remark(
                    passport,
                    CURRENT_USER,
                    editText.getText().toString(),
                    ThisApplication.getCurrentTime(),
                    allPics
            );

            Call<Remark> call = remarkApi.create(remark);
            try {
                savedRemark = call.execute().body();
            } catch (IOException e) {
                AppWarnings.showAlert_NoConnection(RemarksEditorActivity.this);
                e.printStackTrace();
            }

            return savedRemark;
        }

        private void uploadPicToDataBase(Uri uri, Pic pic) {
            try {
                String fileNewName = pic.getId() + "." + "jpg";

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] draftBytes = baos.toByteArray();

                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), draftBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileNewName, requestBody);
                FileApiInterface fileApi = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);
                Call<Void> uploadCall = fileApi.upload("pics", body);
                uploadCall.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Remark savedRemark) {
            super.onPostExecute(savedRemark);
            Intent data = new Intent();
            data.putExtra(NEW_REMARK, savedRemark);
            setResult(RESULT_OK, data);
            finish();
        }

    }


}