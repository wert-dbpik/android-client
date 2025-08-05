package ru.wert.tubus_mobile.viewer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

import ru.wert.tubus_mobile.R;


public class PdfViewerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);

//        ((Activity) inflater.getContext()).getWindow().setNavigationBarColor(getResources().getColor(R.color.colorWhite, null));

        TextView warning = v.findViewById(R.id.tvPdfStatusWarning);
        ((ViewerActivity)getActivity()).showStatusWarningIfNeeded(warning);

        File localFile = null;
        Bundle bundle = this.getArguments();
        if(bundle != null)
            localFile = new File(bundle.getString("LOCAL_FILE"));

        PDFView pdfView = v.findViewById(R.id.pdfView);

        pdfView.fromFile(localFile)
                .enableSwipe(true)
                .swipeHorizontal(false) // Вертикальный скролл
                .pageFitPolicy(FitPolicy.BOTH)
                .fitEachPage(true)
                .defaultPage(0)
                .load();

        return v;
    }

}