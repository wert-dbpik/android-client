package ru.wert.bazapik_mobile.viewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

import ru.wert.bazapik_mobile.R;


public class PdfViewerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);

        File localFile = null;
        Bundle bundle = this.getArguments();
        if(bundle != null)
            localFile = new File(bundle.getString("LOCAL_FILE"));

        PDFView pdfView = v.findViewById(R.id.pdfView);

        pdfView.fromFile(localFile)
                .defaultPage(0)
                .pageFitPolicy(FitPolicy.WIDTH)
                .fitEachPage(true)
                .defaultPage(0)
                .load();

        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });

        return v;
    }

}