package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.serviceNew.files.FileRetrofitService;
import ru.wert.bazapik_mobile.data.serviceNew.files.PicRetrofitService;
import ru.wert.bazapik_mobile.data.serviceNew.files.RemarkRetrofitService;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkEditorFragment extends Fragment implements
        RemarkRetrofitService.IRemarkCreator, RemarkRetrofitService.IRemarkChanger,
        FileRetrofitService.IFileUploader, PicRetrofitService.IPicCreator {

    @Getter private EditText editor;
    @Getter private Button btnAdd;

    private final String TAG = "RemarkFragment";
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";

    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private Context context;

    @Setter private Remark changedRemark;

    private IRemarkFragmentInteraction viewInteraction;

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
    public void doWhenPicIsCreated(Response<Pic> response, Uri uri) {
        try {
            Pic savedPic = (Pic) response.body();
            String fileNewName = savedPic.getId() + "." + savedPic.getExtension();
            InputStream iStream;
            iStream = context.getContentResolver().openInputStream(uri);
            byte[] draftBytes = ThisApplication.getBytes(iStream);
            FileRetrofitService.uploadFile(RemarkEditorFragment.this, context, "pics", fileNewName, draftBytes);
            //Смотри doWhenFileIsUploaded
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override //FileRetrofitService.IFileUploader
    public void doWhenFileIsUploaded() {
        viewInteraction.updateRemarkAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.fragment_remark_editor, container, false);

        editor = view.findViewById(R.id.etTextRemark);
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

        return view;
    }

    private void addRemark(){

        Remark remark = new Remark(
                viewInteraction.getPassport(),
                CURRENT_USER,
                editor.getText().toString(),
                ThisApplication.getCurrentTime()
        );

        RemarkRetrofitService.create(RemarkEditorFragment.this, context, remark);
        //Смотри doWhenRemarkIsCreated

    }

    @Override//RemarkRetrofitService.IRemarkCreator
    public void doWhenRemarkIsCreated(Response<Remark> response) {
        viewInteraction.closeRemarkFragment();
        viewInteraction.updateRemarkAdapter();
        viewInteraction.findPassportById(viewInteraction.getPassport().getId())
                .getRemarkIds().add(response.body().getId());
    }


    private void changeRemark(){

        changedRemark.setUser(CURRENT_USER);
        changedRemark.setText(editor.getText().toString());
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