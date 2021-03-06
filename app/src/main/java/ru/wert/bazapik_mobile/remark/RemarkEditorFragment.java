package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.FileRetrofitService;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.PicRetrofitService;
import ru.wert.bazapik_mobile.data.serviceRETROFIT.RemarkRetrofitService;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.pics.PicsAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkEditorFragment extends Fragment implements
        FileRetrofitService.IFileUploader, PicRetrofitService.IPicCreator {

    @Getter private EditText textEditor;
    @Getter private TextView tvTitle;
    @Getter private Button btnAdd;

    private final String TAG = "RemarkFragment";
    public static final String sAdd = "????????????????";
    public static final String sChange = "????????????????";

    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private Context context;
    private InfoActivity activity;

    private IRemarkFragmentInteraction viewInteraction;
    private RecyclerView rvEditorRemarkPics;

    @Getter private PicsAdapter picsAdapter;
    @Setter private List<Pic> picsInAdapter;
    private Bundle resumeBundle;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String REMARK_TEXT = "remark_text";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (InfoActivity) context;
        viewInteraction = (IRemarkFragmentInteraction) context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickUpPictureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null) {
                            List<Uri> chosenPics;
                            ClipData clipData = data.getClipData();
                            chosenPics = (clipData == null ?
                                    Collections.singletonList(data.getData()) :
                                    ThisApplication.clipDataToList(clipData));

                            for(Uri uri : chosenPics){
                                String ext;
                                String mimeType = context.getContentResolver().getType(uri);
                                if(mimeType.startsWith("image")) {
                                    String str = mimeType.split("/", -1)[1];
                                    ext = str.equals("jpeg") ? "jpg" : str;
                                } else
                                    return;
                                PicRetrofitService.create(RemarkEditorFragment.this, context, uri, ext);
                                //???????????? doWhenPicIsCreated

                            }
                        }
                    }
                });
    }

    private Bundle createSaveStateBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(REMARK_TEXT, textEditor.getText().toString());

        Parcelable listState = Objects.requireNonNull(rvEditorRemarkPics.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(resumeBundle == null) return;
        activity.runOnUiThread(()->{
            textEditor.setText(resumeBundle.getString(REMARK_TEXT));
            textEditor.setSelection(textEditor.length());

            Parcelable savedRecyclerLayoutState = resumeBundle.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(rvEditorRemarkPics.getLayoutManager()).onRestoreInstanceState(savedRecyclerLayoutState);
            viewInteraction.getRemarkContainerView().setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        resumeBundle = createSaveStateBundle();
    }

    @Override
    public void onPause() {
        super.onPause();
        resumeBundle = createSaveStateBundle();
    }

    @Override//IPicCreator
    public void doWhenPicHasBeenCreated(Response<Pic> response, Uri uri) {
        //?????????????????? ?????????????????? ???????????????? ?? ?????????????????? ?????? ????????????????
        picsInAdapter.add(response.body());
        try {
            Pic savedPic = response.body();
            String fileNewName = savedPic.getId() + "." + "jpg";

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] draftBytes = baos.toByteArray();

            FileRetrofitService.uploadFile(RemarkEditorFragment.this, context, "pics", fileNewName, draftBytes);
            //doWhenFileHasBeenUploaded
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override //FileRetrofitService.IFileUploader
    public void doWhenFileHasBeenUploaded() {

        picsAdapter.changeListOfItems(new ArrayList<>(picsInAdapter));

//        viewInteraction.updateRemarkAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.fragment_remark_editor, container, false);

        textEditor = view.findViewById(R.id.etTextRemark);
        tvTitle = view.findViewById(R.id.tvRemarkTitle);
        btnAdd = view.findViewById(R.id.btnAddRemark);
        btnAdd.setOnClickListener(v->{
            if(btnAdd.getText().equals(sAdd))
                addRemark();
            else
                changeRemark();
        });

        ImageButton btnAddImage = view.findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(v ->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            pickUpPictureResultLauncher.launch(intent);
        });

        rvEditorRemarkPics = view.findViewById(R.id.rvEditorRemarkPics);
        picsInAdapter = new ArrayList<>();
        fillRecViewWithPics(picsInAdapter);

        return view;
    }

    private void fillRecViewWithPics(List<Pic> pics) {
        rvEditorRemarkPics.setLayoutManager(new LinearLayoutManager(getContext()));
        picsAdapter = new PicsAdapter(context, pics, PicsAdapter.EDITOR_FRAGMENT, this);
        rvEditorRemarkPics.setAdapter(picsAdapter);
        rvEditorRemarkPics.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    private void addRemark(){
        activity.getRm().addRemark(picsInAdapter);
    }


    private void changeRemark(){

        activity.getRm().changeRemark(picsInAdapter);
    }

    /**
     * ?????????? ?????????????? ???? ?????????????????? ?????????? ?? ??????????????????????
     */
    public void clearRemarkEditor() {
        picsInAdapter = new ArrayList<>();
        picsAdapter.changeListOfItems(picsInAdapter);
        textEditor.setText("");
    }

    /**
     * ???? ??????????????
     */
    private void archive(){
//            1) ?????????????????????? ?????????????????????? 1:1 - ???????????? ????????????????
//            InputStream iStream;
//            iStream = context.getContentResolver().openInputStream(uri);
//            byte[] draftBytes = ThisApplication.getBytes(iStream);

//            2) ?????????? ????????????????????????????  ???????????? ScalingUtilities ?? ?????????????????? ?????????????? ?????? ????????????????,
//            ???????????? ?????????? ???????????????????????? ?? ?????????????????? bitmap ???????????????????? ???????? ???????????? 250????
//            ???? ???????????????????????? ?? ???????????????????????? windows
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//            Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(bitmap, 600, 600, ScalingUtilities.ScalingLogic.FIT);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] draftBytes = baos.toByteArray();

//            3) ?????????? ???????????? ?????????????? ?????????? 195????,
//            ???? ???????????????????????? ?? ???????????????????????? windows
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//            byte[] draftBytes = baos.toByteArray();
    }

}