package ru.wert.bazapik_mobile.viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import ru.wert.bazapik_mobile.R;

/**
 * Фрагмент для отображения файлов типа JPEG, PNG и т.д.
 */
public class ImageViewer extends Fragment {

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView mDraftImageView;
    private int _xDelta;
    private int _yDelta;
    private FrameLayout mImageViewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mDraftImageView = v.findViewById(R.id.draftImageView);
        mImageViewContainer = v.findViewById(R.id.image_view_container);

        Bundle bundle = this.getArguments();
        File localFile;
        if(bundle != null) {
            localFile = new File(this.getArguments().getString("LOCAL_FILE"));
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            mDraftImageView.setImageBitmap(bitmap);
        }

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(mImageViewContainer.getWidth(), mImageViewContainer.getHeight());
        mDraftImageView.setLayoutParams(layoutParams);
        mScaleGestureDetector = new ScaleGestureDetector(container.getContext(), new ScaleListener());

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

//                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//                    mScaleGestureDetector.onTouchEvent(motionEvent);
//                }
                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        _xDelta = X - lParams.leftMargin;
                        _yDelta = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:

//                        mScaleGestureDetector.onTouchEvent(motionEvent);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = X - _xDelta;
                        layoutParams.topMargin = Y - _yDelta;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        view.setLayoutParams(layoutParams);
                        break;
                }
                view.invalidate();


                return true;
            }
        });

        return v;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            mDraftImageView.setScaleX(mScaleFactor);
            mDraftImageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}