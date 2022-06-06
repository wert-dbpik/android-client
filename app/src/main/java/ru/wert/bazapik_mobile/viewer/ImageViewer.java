package ru.wert.bazapik_mobile.viewer;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import org.apache.commons.io.FileUtils;

import androidx.fragment.app.Fragment;

import java.io.File;

import ru.wert.bazapik_mobile.R;

import static ru.wert.bazapik_mobile.ThisApplication.SOLID_EXTENSIONS;
import static ru.wert.bazapik_mobile.ThisApplication.getAppContext;

/**
 * Фрагмент для отображения файлов типа JPEG, PNG и т.д.
 */
public class ImageViewer extends Fragment {

    private ZoomableImageView mDraftImageView;
    private Button btnGo; //Кнопка открывания в стороннем приложении

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mDraftImageView = v.findViewById(R.id.draftImageView);
        btnGo = container.findViewById(R.id.btnGo);


        Bundle bundle = this.getArguments();
        File localFile;
        if(bundle != null) {
            String bundleString = this.getArguments().getString("LOCAL_FILE");
            if(SOLID_EXTENSIONS.contains(FileUtils.getExtension(bundleString))){
                String mimeType = getMimeType(Uri.parse(bundleString));
                if(mimeType == null)
                    mimeType = "application/sldworks";
                ((ViewerActivity)getActivity()).createButtonGo2(bundleString);
                Bitmap bitmap = BitmapFactory.decodeResource(ImageViewer.this.getResources(),
                        R.drawable.image3dpng);
                mDraftImageView.setImageBitmap(bitmap);
            }else {
                ((ViewerActivity)getActivity()).createButtonGo(bundleString, "image/*");
                localFile = new File(bundleString);
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                mDraftImageView.setImageBitmap(bitmap);
            }

        }

        return v;
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = getAppContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }



}