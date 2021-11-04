package ru.wert.bazapik_mobile.viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.utils.FileFwdSlash;
import ru.wert.bazapik_mobile.warnings.Warning1;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;

import static ru.wert.bazapik_mobile.constants.Consts.TEMP_DIR;

/**
 * Активность запускается из класса ItemRecViewAdapter
 * принимает ArrayList<String>, состоящий из id чертежей
 */
public class ViewerActivity extends BaseActivity {

    private static final String TAG = "ViewerActivity";
    private ImageView mImageView;
    private Button mBtnPrevious, mBtnNext;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page curPage;
    private ParcelFileDescriptor descriptor;
    private float currentZoomLevel = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        mImageView = findViewById(R.id.imgView);
        mBtnPrevious = findViewById(R.id.btnPrevious);
        mBtnNext = findViewById(R.id.btnNext);


//        long[] draftIds = getIntent().getLongArrayExtra("draftIds");

        ArrayList<String> list = getIntent().getStringArrayListExtra("draftIds");
        ArrayList<Long> draftIds = ((ArrayList<Long>) list.stream().map(Long::valueOf)
                .collect(Collectors.toList()));

        List<Bitmap> allImages = new ArrayList<>();

//        new Thread(()->{
//            String ext = "";
//            File file = null;
//            for(Long fileId : draftIds) {
//                Draft draft = DRAFT_SERVICE.findById(fileId);
//                ext = draft.getExtension();
//                //Загружаем файл во временную папку
//                file = downloadToTempDir(fileId, ext);
//            }
//            String finalExt = ext;
//            File finalFile = file;
//            runOnUiThread(()->{
//                if(finalExt.equals("pdf")) {
//                    List<Bitmap> imgs = separatePdfToBitmaps(finalFile);
//                    allImages.addAll(imgs);
//                } else {
//                    allImages.add(getBitmap(finalFile.getPath()));
//                }
//
//                mImageView.setImageBitmap(allImages.get(0));
//            });
//        }).start();




    }

    private List<Bitmap> separatePdfToBitmaps(File file) {
        List<Bitmap> imgs = new ArrayList<>();
        return imgs;
    }

    private void openPdfRenderer(File file) {
        descriptor = null;
        pdfRenderer = null;
        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(descriptor);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap getBitmap(String filePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        Boolean scaleByHeight = Math.abs(options.outHeight - 100) >= Math
                .abs(options.outWidth - 100);
        if (options.outHeight * options.outWidth * 2 >= 16384) {
            double sampleSize = scaleByHeight
                    ? options.outHeight / 100
                    : options.outWidth / 100;
            options.inSampleSize =
                    (int) Math.pow(2d, Math.floor(
                            Math.log(sampleSize) / Math.log(2d)));
        }
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[512];
        Bitmap output = BitmapFactory.decodeFile(filePath, options);
        return output;
    }

    /**
     * Загружаем файл во временную папку TEMP_DIR
     */
    public File downloadToTempDir(Long fileId, String ext) {

        File file = null;

        //Если файл отсутствует в папке temp, то файл туда загружается из БД
        if(!draftInTempDir(fileId, ext)) {
            boolean res = DraftQuickService.getInstance().download("drafts", //Постоянная папка в каталоге для чертежей
                    String.valueOf(fileId), //название скачиваемого файла
                    "." + ext, //расширение скачиваемого файла
                    TEMP_DIR.toString()); //временная папка, куда необходимо скачать файл
            if(res) {
                Log.d(TAG, String.format("ShowDraft: файл '%s' загружен c сервера во временную папку", String.valueOf(fileId) + "." + ext));
            } else {
                Log.d(TAG, String.format("createSelectionListener : файл '%s' не был загружен с сервера", String.valueOf(fileId) + "." + ext));
                Warning1.show(ViewerActivity.this, "Внимание!", "Чертеж не доступен!");
                return null;
            }
            file = new File(new FileFwdSlash(TEMP_DIR.toString() + "/" + fileId + "." + ext).toStrong());

        }

        return file;
    }



    /**
     * Проверяет наличие файла во временной папке
     * @param fileId Long id файла совпадает с его именем
     * @param ext String расширение файла
     * @return boolean, true - если файл есть в папке
     */
    private boolean draftInTempDir(Long fileId, String ext) {
        String searchedFileName = fileId.toString() + "." + ext;
        Log.d(TAG, String.format
                ("draftInTempDir: проверяем наличие чертежа %s во временной папке", searchedFileName));
        try {
            Path drafts = TEMP_DIR.toPath();
            List<Path> filesInFolder = Files.walk(drafts)
                    .filter(Files::isRegularFile).collect(Collectors.toList());

            for(Path p : filesInFolder){
                if(p.getFileName().toString().contains(searchedFileName)) {
                    Log.d(TAG, String.format
                            ("draftInTempDir: во временной папке файл %s найден", searchedFileName));
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format
                ("draftInTempDir: во временной папке файл %s НЕ найден", searchedFileName));
        return false;
    }
}