package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import ru.wert.bazapik_mobile.pics.PicsAdapter;
import ru.wert.bazapik_mobile.utils.ScalingUtilities;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkEditorFragment extends Fragment implements
        RemarkRetrofitService.IRemarkCreate, RemarkRetrofitService.IRemarkChange,
        FileRetrofitService.IFileUploader, PicRetrofitService.IPicCreator {

    @Getter private EditText textEditor;
    @Getter private Button btnAdd;

    private final String TAG = "RemarkFragment";
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";

    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private Context context;

    @Setter private Remark changedRemark;

    private IRemarkFragmentInteraction viewInteraction;
    private RecyclerView rvEditorRemarkPics;

    private Set<Pic> picsInAdapter = new HashSet<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
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
                                if(mimeType.startsWith("image"))
                                    ext = mimeType.split("/", -1)[1];
                                else
                                    return;
                                PicRetrofitService.create(RemarkEditorFragment.this, context, uri, ext);
                                //Смотри doWhenPicIsCreated

                            }
                        }
                    }
                });
    }

    @Override//IPicCreator
    public void doWhenPicHasBeenCreated(Response<Pic> response, Uri uri) {
        //Добавляем выбранную картинку в уоллекцию для адаптера
        picsInAdapter.add(response.body());
        try {
            Pic savedPic = response.body();
            String fileNewName = savedPic.getId() + "." + "jpg";

            InputStream iStream;
            iStream = context.getContentResolver().openInputStream(uri);
            byte[] draftBytes = ThisApplication.getBytes(iStream);


//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//            Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(bitmap, 600, 600, ScalingUtilities.ScalingLogic.FIT);
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//            byte[] draftBytes = baos.toByteArray();

            //Добавим изображение в поле под текстом
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(bitmap, 600, 600, ScalingUtilities.ScalingLogic.FIT);

            FileRetrofitService.uploadFile(RemarkEditorFragment.this, context, "pics", fileNewName, draftBytes);
            //Смотри doWhenFileIsUploaded
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override //FileRetrofitService.IFileUploader
    public void doWhenFileHasBeenUploaded() {
        ((PicsAdapter)rvEditorRemarkPics.getAdapter()).changeListOfItems(new ArrayList<>(picsInAdapter));

//        viewInteraction.updateRemarkAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.fragment_remark_editor, container, false);

        textEditor = view.findViewById(R.id.etTextRemark);
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
        rvEditorRemarkPics.setAdapter(new PicsAdapter(context, picsInAdapter));

        return view;
    }

    private void addRemark(){

        Remark remark = new Remark(
                viewInteraction.getPassport(),
                CURRENT_USER,
                textEditor.getText().toString(),
                ThisApplication.getCurrentTime(),
                picsInAdapter
        );

        RemarkRetrofitService.create(RemarkEditorFragment.this, context, remark);
        //Смотри doWhenRemarkIsCreated

    }

    @Override//RemarkRetrofitService.IRemarkCreator
    public void doWhenRemarkHasBeenCreated(Response<Remark> response) {
        viewInteraction.closeRemarkFragment();
        viewInteraction.updateRemarkAdapter();
        viewInteraction.findPassportById(viewInteraction.getPassport().getId())
                .getRemarkIds().add(response.body().getId());
    }


    private void changeRemark(){

        changedRemark.setUser(CURRENT_USER);
        changedRemark.setText(textEditor.getText().toString());
        changedRemark.setCreationTime(ThisApplication.getCurrentTime());

        RemarkRetrofitService.change(RemarkEditorFragment.this, context, changedRemark);
        //Смотри doWhenRemarkHasBeenChanged
    }

    @Override//RemarkRetrofitService.IRemarkChanger
    public void doWhenRemarkHasBeenChanged(Response<Remark> response) {
        viewInteraction.closeRemarkFragment();
        viewInteraction.updateRemarkAdapter();
    }


}