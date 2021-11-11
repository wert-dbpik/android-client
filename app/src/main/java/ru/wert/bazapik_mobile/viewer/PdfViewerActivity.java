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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);


//        String pdfFile = "http://192.168.1.84:8080/drafts/download/drafts/37.pdf";


        Draft draft = new Draft();


    }



    //	reload on resume
    @Override
    protected void onResume() {
        super.onResume();
//        pdfVebView.loadUrl( "javascript:window.location.reload( true )" );

    }

    //	clear cache to ensure we have good reload
    @Override
    protected void onPause() {
        super.onPause();
//        pdfVebView.clearCache(true);

    }
}