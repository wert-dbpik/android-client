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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.pics.PicsUriAdapter;
import ru.wert.bazapik_mobile.pics.RemarkImage;
import ru.wert.bazapik_mobile.viewer.DownloadFileTask;
import ru.wert.bazapik_mobile.warnings.AppWarnings;

import static ru.wert.bazapik_mobile.ThisApplication.DATA_BASE_URL;
import static ru.wert.bazapik_mobile.ThisApplication.REQUEST_CODE_PERMISSION_CAMERA;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;
import static ru.wert.bazapik_mobile.info.InfoActivity.ADD_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.CHANGING_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.NEW_REMARK;
import static ru.wert.bazapik_mobile.info.InfoActivity.REMARK_PASSPORT;
import static ru.wert.bazapik_mobile.info.InfoActivity.TYPE_OF_REMARK_OPERATION;

public class RemarksEditorActivity extends BaseActivity {

    private final String TAG = "RemarkFragment";

    //Картинки в ресайклере как сохраненные, так и несохраненные
    @Setter private List<RemarkImage> imagesInAdapter;

    private Passport passport;
    private String text;
    private List<Pic> pics;
    private RecyclerView rvEditorRemarkPics;
    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private ActivityResultLauncher<Intent> takePhotoResultLauncher;
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";
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

        //Проверка на предоставленное разрешение пользоваться камерой
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // разрешение не предоставлено
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
        }

        //Инициализация состояния в зависимости от проводимой операции
        Intent intent = getIntent();
        typeOfRemarkOperation = intent.getIntExtra(TYPE_OF_REMARK_OPERATION, 1);
        passport = intent.getParcelableExtra(REMARK_PASSPORT);

        if (typeOfRemarkOperation == ADD_REMARK) {
            tvTitle.setText("Добавление комментария");
            btnAdd.setText(sAdd);
            imagesInAdapter = new ArrayList<>();
            fillRecViewWithImages();
        } else { //CHANGE_REMARK
            changingRemark = intent.getParcelableExtra(CHANGING_REMARK);
            tvTitle.setText("Изменение комментария");
            btnAdd.setText(sChange);
            editText.setText(changingRemark.getText());
            imagesInAdapter = new ArrayList<>();
            fillRecViewWithCashedPics(changingRemark.getPicsInRemark());
        }

        //ДОБАВИТЬ или ИЗМЕНИТЬ КОММЕНТАРИЙ
        btnAdd.setOnClickListener(v -> {
            AsyncTask<List<RemarkImage>, Void, Remark> addRemark = new SaveRemarkTask();
            addRemark.execute(imagesInAdapter);
        });

        //ДОБАВИТЬ КАРТИНКУ ИЗ ГАЛЕРЕЙ -------------------------------------------------------
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
                            for(Uri u : chosenPics) {
                                imagesInAdapter.add(new RemarkImage(u, null));
                            }
                            fillRecViewWithImages();
                        }
                    }
                });


        btnAddImage.setOnClickListener(v -> {
            Intent addImageIntent = new Intent();
            addImageIntent.setAction(Intent.ACTION_GET_CONTENT);
            addImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            addImageIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            pickUpPictureResultLauncher.launch(addImageIntent);
        });

        //ДОБАВИТЬ ФОТО ----------------------------------------------------------------------

        takePhotoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imagesInAdapter.add(new RemarkImage(imageUri, null));
                        fillRecViewWithImages();
                    }
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

    /**
     * Метод, получив сохраненные картинки, загружает их в кэш и полученный Uri файлов добавляет в imagesInAdapter.
     * Метод вызывается при изменении комментария только в начале
     * После вызывается fillRecViewWithImages, куда передаются полученные результаты.
     * @param picsInRemark, List<Pic>
     */
    private void fillRecViewWithCashedPics(List<Pic> picsInRemark) {
        for(Pic pic: picsInRemark){
            try {
                //Путь к удаленному файлу
                String remoteFileString = DATA_BASE_URL + "drafts/download/pics/" + pic.getName();
                // временная папка
                File outputDir = RemarksEditorActivity.this.getCacheDir();
                //Создание временного файла
                File outputFile = File.createTempFile("remark_pic_" + pic.getId(), pic.getExtension(), outputDir);
                //Загружаем изображение из БД во временную папку
                String res = new DownloadFileTask().execute(remoteFileString, outputFile.toString()).get();
                if(res.equals("OK"))
                    //Добавляем итоговый Uri в список изображений
                    imagesInAdapter.add(new RemarkImage(Uri.fromFile(outputFile), pic));

            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        fillRecViewWithImages();
    }

    /**
     * В методе создает файл, куда будет сохранена фотография, полученная с камеры телефона
     * @return image_file, File
     */
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

        return image_file;
    }

    /**
     * Метод устанавливает адаптер со списком изображений imagesInAdapter
     */
    private void fillRecViewWithImages() {
        rvEditorRemarkPics.setLayoutManager(new LinearLayoutManager(this));
        picsUriAdapter = new PicsUriAdapter(this, imagesInAdapter, PicsUriAdapter.REMARK_EDITOR, this);
        rvEditorRemarkPics.setAdapter(picsUriAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
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

    /**
     * Класс - Задача на сохранение как нового комментария, так и на изменение старого
     */
    private class SaveRemarkTask extends AsyncTask<List<RemarkImage>, Void, Remark> {

        Remark targetRemark;

        @SafeVarargs
        @Override
        protected final Remark doInBackground(List<RemarkImage>... images) {

            //Сохраняем и добавляем новые картинки
            PicApiInterface picApi = RetrofitClient.getInstance().getRetrofit().create(PicApiInterface.class);

            List<Pic> newPics = new ArrayList<>();
            List<RemarkImage> allImages = images[0];
            //Выбираем из картинок ранее сохраненные
            List<Pic> allPics = allImages.stream().map(RemarkImage::getPic).filter(Objects::nonNull).collect(Collectors.toList());
            //Выбираем из картинок только новые
            List<Uri> allUris = allImages.stream().filter(image -> image.getPic() == null).map(RemarkImage::getUri).collect(Collectors.toList());

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

                    newPics.add(pic);
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка декодирования файла: " + e.getMessage());
                }

            }

            allPics.addAll(newPics);

            if(typeOfRemarkOperation == ADD_REMARK)
                addRemark(allPics);
            else
                changeRemark(allPics);

            return targetRemark;
        }

        /**
         * Метод сохраняет изображение в БД
         * @param allPics
         */
        private void addRemark(List<Pic> allPics) {
            Remark remark = new Remark(
                    passport,
                    CURRENT_USER,
                    editText.getText().toString(),
                    ThisApplication.getCurrentTime(),
                    allPics
            );

            RemarkApiInterface remarkApi = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
            Call<Remark> call = remarkApi.create(remark);
            try {
                targetRemark = call.execute().body();
            } catch (IOException e) {
                AppWarnings.showAlert_NoConnection(RemarksEditorActivity.this);
                e.printStackTrace();
            }
        }

        /**
         * Метод изменяет изображение в БД
         * @param allPics
         */
        private void changeRemark(List<Pic> allPics) {
            changingRemark.setUser(CURRENT_USER);
            changingRemark.setText(editText.getText().toString());
            changingRemark.setCreationTime(ThisApplication.getCurrentTime());
            changingRemark.setPicsInRemark(allPics);

            RemarkApiInterface remarkApi = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
            Call<Remark> call = remarkApi.update(changingRemark);

            try {
                targetRemark = call.execute().body();
            } catch (IOException e) {
                AppWarnings.showAlert_NoConnection(RemarksEditorActivity.this);
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Remark changedRemark) {
            super.onPostExecute(changedRemark);
            Intent data = new Intent();
            data.putExtra(NEW_REMARK, changedRemark);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * Метод после создания записи Pic в БД загружает само изображение
     * @param uri
     * @param pic
     */
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


}