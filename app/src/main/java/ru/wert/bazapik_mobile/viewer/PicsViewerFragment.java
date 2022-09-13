package ru.wert.bazapik_mobile.viewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import ru.wert.bazapik_mobile.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;

public class PicsViewerFragment extends Fragment {

    public static final String PATH_TO_PIC = "path_to_pic";
    private Bitmap bitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pics_viewer, container, false);
        ZoomableImageView mDraftImageView = v.findViewById(R.id.picsView);

//        ((Activity) inflater.getContext()).getWindow().setNavigationBarColor(getResources().getColor(R.color.colorWhite, null));

        new Thread(() -> {
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                String pathToPic = bundle.getString(PATH_TO_PIC);

                try {
                    bitmap = Picasso.get().load(Uri.parse(pathToPic)).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((Activity) inflater.getContext()).runOnUiThread(() -> {
                    mDraftImageView.setImageBitmap(bitmap);
                });
            }
        }).start();


        return v;
    }
}