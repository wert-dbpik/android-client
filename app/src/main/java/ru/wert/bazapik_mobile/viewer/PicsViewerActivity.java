package ru.wert.bazapik_mobile.viewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.organizer.AppOnSwipeTouchListener;
import ru.wert.bazapik_mobile.utils.AnimationDest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;

import static ru.wert.bazapik_mobile.data.retrofit.RetrofitClient.BASE_URL;
import static ru.wert.bazapik_mobile.viewer.PicsViewerFragment.PATH_TO_PIC;

public class PicsViewerActivity extends AppCompatActivity {

    private final String TAG = "PicsViewer";

    public static final String ALL_PICS = "all_pics";
    public static final String CURRENT_PIC = "current_pic";
    public static final String WHO_CALL_ME = "resource";
    public static final String SINGLE_URI = "single_uri";

    private List<Pic> allPics;
    private Pic currentPic;
    private AnimationDest destination = AnimationDest.ANIMATE_NEXT; //Влияет на анимацию
    private Integer iterator; //Текущая позиция
    private Button btnTapLeft, btnTapRight;
    private ImageButton btnShowPrevious, btnShowNext;
    private final FragmentManager fm = getSupportFragmentManager();
    private String source;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_viewer);

        btnShowPrevious = findViewById(R.id.btnShowPrevious);
        btnShowNext = findViewById(R.id.btnShowNext);
        btnTapLeft = findViewById(R.id.btnTapLeft);
        btnTapRight = findViewById(R.id.btnTapRight);

        intent = getIntent();
        allPics = intent.getParcelableArrayListExtra(ALL_PICS);
        source = intent.getStringExtra(WHO_CALL_ME);

        //Из редактора открывается только одна картинка за раз,
        //поэтому не нужен итератор и не нужны кнопки навигации
        if(source == null || !source.equals("editor")) {
            currentPic = intent.getParcelableExtra(CURRENT_PIC);

            //Получаем текущую позицию рисунка
            initIterator();

            btnShowPrevious.setOnClickListener(showPreviousDraft());
            btnShowNext.setOnClickListener(showNextDraft());
            btnTapLeft.setOnTouchListener(createOnSwipeTouchListener());
            btnTapRight.setOnTouchListener(createOnSwipeTouchListener());
        }

        openFragment();
    }

    private void initIterator() {
        for(int i = 0; i < allPics.size(); i++){
            Pic pic = allPics.get(i);
            if(pic.getId().equals(currentPic.getId())) {
                iterator = i;
                break;
            }
        }
    }

    private void openFragment() {
        String pathToPic;
        if(source == null || !source.equals("editor")) {
            if (iterator.equals(0))
                switchOffButton(btnShowPrevious);
            else
                switchOnButton(btnShowPrevious);

            if (iterator.equals(allPics.size() - 1))
                switchOffButton(btnShowNext);
            else
                switchOnButton(btnShowNext);

            pathToPic = BASE_URL + "files/download/pics/" + currentPic.getId() + "." + currentPic.getExtension();
        } else {
            switchOffButton(btnShowPrevious);
            switchOffButton(btnShowNext);
            pathToPic = intent.getStringExtra(SINGLE_URI);
        }

        Bundle bundle = new Bundle();
        bundle.putString(PATH_TO_PIC, pathToPic);

        Fragment picsViewerFragment = new PicsViewerFragment();
        picsViewerFragment.setArguments(bundle);
        FragmentTransaction ft = fm.beginTransaction();
        if (destination.equals(AnimationDest.ANIMATE_NEXT))
            ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        else
            ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.pics_viewer_fragment_container, picsViewerFragment);
        ft.commit();
    }

    private void switchOnButton(ImageButton btn){
        btn.setVisibility(View.VISIBLE);
        btn.setClickable(true);
    }

    private void switchOffButton(ImageButton btn){
        btn.setVisibility(View.INVISIBLE);
        btn.setClickable(false);
    }

    public AppOnSwipeTouchListener createOnSwipeTouchListener(){
        return new AppOnSwipeTouchListener(PicsViewerActivity.this){
            public void onSwipeRight() {
                if(iterator - 1 < 0) return;
                currentPic = allPics.get(--iterator);
                destination = AnimationDest.ANIMATE_PREV;
                openFragment();
            }
            public void onSwipeLeft() {
                if(iterator + 1 > allPics.size()-1) return;
                currentPic = allPics.get(++iterator);
                destination = AnimationDest.ANIMATE_NEXT;
                openFragment();
            }
        };
    }

    private View.OnClickListener showNextDraft() {
        return v -> {
            currentPic = allPics.get(++iterator);
            destination = AnimationDest.ANIMATE_NEXT;
            openFragment();
        };
    }

    private View.OnClickListener showPreviousDraft() {
        return v -> {
            currentPic = allPics.get(--iterator);
            destination = AnimationDest.ANIMATE_PREV;
            openFragment();
        };
    }
}