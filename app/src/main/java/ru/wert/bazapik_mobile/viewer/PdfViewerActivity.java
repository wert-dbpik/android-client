package ru.wert.bazapik_mobile.viewer;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.utils.TempDirectory;
import ru.wert.bazapik_mobile.data.models.Draft;

public class PdfViewerActivity extends BaseActivity {
    public static final String TAG = "PdfViewerActivity";
    WebView pdfVebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

//        pdfVebView = findViewById(R.id.pdfWebview);

        WebSettings settings = pdfVebView.getSettings();
        settings.setJavaScriptEnabled(true);


        settings.setAllowUniversalAccessFromFileURLs(true);

        settings.setBuiltInZoomControls(true);
        pdfVebView.setWebChromeClient(new WebChromeClient());
        pdfVebView.loadUrl("file:///android_asset/pdf_js/viewer.html");
//        pdfVebView.loadUrl("http//192.168.84.1:8080/drafts/download/drafts/37.pdf");

//        String pdfFile = "http://192.168.1.84:8080/drafts/download/drafts/37.pdf";

//        Draft draft = DraftQuickService.getInstance().findById(37L);
        Draft draft = new Draft();
//        draft.setId(37L);
//        draft.setExtension("pdf");

        AtomicReference<String> imageData = new AtomicReference<>("");
        new Thread(()->{
            TempDirectory.getInstance(getAppContext()).downloadDraft(draft);

            try {
                String path = TempDirectory.getInstance(getAppContext()).getTempDir().toString() + draft.getId() + "."+ draft.getExtension();
                showToast(path);
                Log.d(TAG, "Путь до временного файла " + path);
                URL url = new URL(path);
                InputStream is = url.openStream();
                byte[] data = readBytes(is);

                imageData.set(Base64.getEncoder().encodeToString(data));
                pdfVebView.loadUrl("javascript:openFileFromBase64('" + imageData.get() + "')");
                showToast("Строка :" + imageData.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });




    }

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while (inputStream.available() >0 && (len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

//    public void showPDF(File file) {
//
//        try {
//            byte[] data = FileUtils..readFileToByteArray(file);
//            //Base64 from java.util
//            String base64 = Base64.getEncoder().encodeToString(data);
//            //This must be ran on FXApplicationThread
//            pdfVebView.loadUrl("openFileFromBase64('" + base64 + "')");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //	reload on resume
    @Override
    protected void onResume() {
        super.onResume();
        pdfVebView.loadUrl( "javascript:window.location.reload( true )" );

    }

    //	clear cache to ensure we have good reload
    @Override
    protected void onPause() {
        super.onPause();
        pdfVebView.clearCache(true);

    }
}