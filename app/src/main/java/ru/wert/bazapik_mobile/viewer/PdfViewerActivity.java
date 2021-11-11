package ru.wert.bazapik_mobile.viewer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.utils.TempDirectory;

public class PdfViewerActivity extends BaseActivity {
    public static final String TAG = "PdfViewerActivity";

    private Long draftId;
    private PDFView pdfView;
    private TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        tvTest = findViewById(R.id.tvTest);
        pdfView = findViewById(R.id.pdfView);

        draftId = Long.parseLong(getIntent().getStringExtra("DRAFT_ID"));

        tvTest.setText(String.valueOf(draftId));

//        File tempDir = TempDirectory.getInstance(PdfViewerActivity.this).getTempDir();
//        new Thread(()->{
////            Draft draft = DraftService.getInstance().findById(draftId);
////            DraftService.getInstance().download("drafts",
////                    String.valueOf(draftId),
////                    "." + draft.getExtension(),
////                    tempDir.getPath());
//            runOnUiThread(()->{
//                tvTest.setText(String.valueOf(draftId));
//            });
//
//        }).start();



//        String pdfFile = "http://192.168.1.84:8080/drafts/download/drafts/37.pdf";





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