package ru.wert.bazapik_mobile.viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
            String bundleString = this.getArguments().getString("LOCAL_FILE");
            if(bundleString.equals("Solid")){

                Bitmap bitmap = BitmapFactory.decodeResource(ImageViewer.this.getResources(),
                        R.drawable.noimage);

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