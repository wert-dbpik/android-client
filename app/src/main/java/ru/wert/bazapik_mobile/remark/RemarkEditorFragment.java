package ru.wert.bazapik_mobile.remark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.PicApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.RemarkApiInterface;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.remark.IRemarkFragmentInteraction;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class RemarkEditorFragment extends Fragment {

    @Getter private EditText editor;
    @Getter private Button btnAdd;

    private String TAG = "RemarkFragment";
    public static final String sAdd = "добавить";
    public static final String sChange = "изменить";

    private ActivityResultLauncher<Intent> pickUpPictureResultLauncher;
    private static final int SELECT_PICTURE = 1;

    @Setter private Remark changedRemark;

    private IRemarkFragmentInteraction viewInteraction;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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
                        if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction()) && data.hasExtra(Intent.EXTRA_STREAM)) {
                            ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                            if (list != null) {
                                for (Parcelable parcel : list) {
                                    Uri uri = (Uri) parcel;
                                    String ext = FileUtils.getExtension(uri.getPath().toLowerCase());
                                    Pic newPic = new Pic();
                                    newPic.setExtension(ext);
                                    newPic.setUser(CURRENT_USER);

                                    PicApiInterface api = RetrofitClient.getInstance().getRetrofit().create(PicApiInterface.class);
                                    Call<Pic> call = api.create(newPic);
                                    call.enqueue(new Callback<Pic>() {
                                        @SneakyThrows
                                        @Override
                                        public void onResponse(Call<Pic> call, Response<Pic> response) {
                                            Pic savedPic = response.body();
                                            String fileNewName = savedPic.getId() + "." + savedPic.getExtension();
                                            File picFile = new File(new URI(uri.toString()));
                                            byte[] draftBytes = Files.readAllBytes(picFile.toPath());
                                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/pdf"), draftBytes);
                                            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileNewName, picFile);
                                            FileApiInterface fileApi = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);
                                            Call<Void> uploadCall = fileApi.upload("pic",
                                                    String.valueOf(savedPic.getId()),
                                                    picFile);
                                        }

                                        @Override
                                        public void onFailure(Call<Pic> call, Throwable t) {

                                        }
                                    });

                                }
                            }
                        }
                    }
                });
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

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.create(remark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    viewInteraction.closeRemarkFragment();
                    viewInteraction.updateRemarkAdapter();
                    viewInteraction.findPassportById(viewInteraction.getPassport().getId())
                            .getRemarkIds().add(response.body().getId());
                } else {
                    Log.d(TAG, String.format("Не удалось сохранить запись, %s", response.message()));
                    new WarningDialog1().show(getActivity(), "Ошибка!","Не удалось сохранить запись");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                Log.d(TAG, String.format("Не удалось сохранить запись, %s", t.getMessage()));
                new WarningDialog1().show(getActivity(), "Ошибка!", "Не удалось сохранить запись");
            }
        });

    }


    private void changeRemark(){

        changedRemark.setUser(CURRENT_USER);
        changedRemark.setText(editor.getText().toString());
        changedRemark.setCreationTime(ThisApplication.getCurrentTime());

        RemarkApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RemarkApiInterface.class);
        Call<Remark> call = api.update(changedRemark);
        call.enqueue(new Callback<Remark>() {
            @Override
            public void onResponse(Call<Remark> call, Response<Remark> response) {
                if(response.isSuccessful()){
                    viewInteraction.closeRemarkFragment();
                    viewInteraction.updateRemarkAdapter();
                } else {
                    Log.d(TAG, String.format("Не удалось изменить запись, %s", response.message()));
                    new WarningDialog1().show(getActivity(), "Ошибка!","Не удалось сохранить запись");
                }
            }

            @Override
            public void onFailure(Call<Remark> call, Throwable t) {
                Log.d(TAG, String.format("Не удалось изменить запись, %s", t.getMessage()));
                new WarningDialog1().show(getActivity(), "Ошибка!", "Не удалось сохранить запись");
            }
        });
    }
}