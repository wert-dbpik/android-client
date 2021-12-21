package ru.wert.bazapik_mobile.viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import java.io.File;

import ru.wert.bazapik_mobile.R;

/**
 * Фрагмент для отображения файлов типа JPEG, PNG и т.д.
 */
public class ImageViewer extends Fragment {

    private ZoomableImageView mDraftImageView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mDraftImageView = v.findViewById(R.id.draftImageView);

        Bundle bundle = this.getArguments();
        File localFile;
        if(bundle != null) {
            localFile = new File(this.getArguments().getString("LOCAL_FILE"));
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            mDraftImageView.setImageBitmap(bitmap);
        }

        return v;
    }


}