package ru.wert.bazapik_mobile.viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import androidx.fragment.app.Fragment;

import java.io.File;

import ru.wert.bazapik_mobile.R;

import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;

/**
 * Фрагмент для отображения файлов типа JPEG, PNG и т.д.
 */
public class ImageViewerFragment extends Fragment {

    private ZoomableImageView mDraftImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mDraftImageView = v.findViewById(R.id.draftImageView);

        TextView warning = v.findViewById(R.id.tvStatusWarning);
        ((ViewerActivity)getActivity()).showStatusWarningIfNeeded(warning);

        Bundle bundle = this.getArguments();
        File localFile;
        if(bundle != null) {
            String bundleString = this.getArguments().getString("LOCAL_FILE");
            if(SOLID_EXTENSIONS.contains(FileUtils.getExtension(bundleString))){
                Bitmap bitmap = BitmapFactory.decodeResource(ImageViewerFragment.this.getResources(),
                        R.drawable.image3dpng);
                mDraftImageView.setImageBitmap(bitmap);
            }else {
                localFile = new File(bundleString);
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                mDraftImageView.setImageBitmap(bitmap);
            }

        }

        return v;
    }



}