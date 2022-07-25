package ru.wert.bazapik_mobile.pics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.remark.RemarkEditorFragment;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;

public class PicsAdapter extends RecyclerView.Adapter<PicsAdapter.ViewHolder>{

    private final String TAG = "PicsAdapter";
    private final LayoutInflater inflater;
    private List<Pic> data;
    private final Context context;
    private final Activity activity;

    RemarkEditorFragment editor;

    private int whoCallMe;
    public static final int INFO_ACTIVITY = 0;
    public static final int EDITOR_FRAGMENT = 1;

    public PicsAdapter(Context context, List<Pic> data, int whoCallMe, RemarkEditorFragment editor) {
        this(context, data, whoCallMe);
        this.editor = editor;
    }

    public PicsAdapter(Context context, List<Pic> data, int whoCallMe) {
        this.context = context;
        this.data = new ArrayList<>(data);
        this.whoCallMe = whoCallMe;

        this.inflater = LayoutInflater.from(context);
        this.activity = (Activity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recview_pic_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Pic pic = data.get(position);

        FileApiInterface api = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);
        String picName = pic.getId() + "." + pic.getExtension();
        Call<ResponseBody> call = api.download("pics", picName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    new Thread(()->{
                        try {
                            byte [] bt = response.body().bytes();
                            activity.runOnUiThread(()->{
                                Bitmap bmp = null;
                                LinearLayout.LayoutParams lParams = null;
                                try {
                                    bmp = BitmapFactory.decodeByteArray(bt, 0 , bt.length);
                                    lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    float w = bmp.getWidth();
                                    float h = bmp.getHeight();
                                    if(w - h < w * 0.1f)
                                        lParams.weight = 0.6f;
                                    else if(w - h > w * 0.1f)
                                        lParams.weight = 0.9f;
                                    else
                                        lParams.weight = 0.75f;
                                } catch (Exception e) {
                                    Log.e(TAG, "Ошибка декодирования файла: " + e.getMessage());
                                    return;
                                }
                                holder.ivPicture.setLayoutParams(lParams);
                                holder.ivPicture.setImageBitmap(bmp);
                                holder.ivPicture.setAdjustViewBounds(true);
                                if (whoCallMe == EDITOR_FRAGMENT)
                                    holder.ivPicture.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                                        PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT, R.attr.actionOverflowMenuStyle, 0);
                                        popup.getMenuInflater().inflate(R.menu.picture_context_menu, popup.getMenu());
                                        popup.setOnMenuItemClickListener(item1 -> {
                                            Pic picture = (Pic) data.get(position);
                                            switch (item1.getItemId()) {
                                                case R.id.deletPicture:
                                                    deletePicture(picture, position);
                                                    break;
                                            }
                                            return true;
                                        });
                                        popup.show();
                                    });

                            });
                        } catch (IOException e) {
                            Log.e(TAG, "Ошибка декодирования файла: " + e.getMessage());
                        }
                    }).start();

                } else {
                    Log.e(TAG, "Couldn't download picture");
                    Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.noimage);
                    holder.ivPicture.setAdjustViewBounds(true);
                    holder.ivPicture.setImageBitmap(bmp);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Couldn't download picture");
                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.noimage);
                holder.ivPicture.setImageBitmap(bmp);
            }
        });

    }

    private void deletePicture(Pic picture, int position){
        data.remove(picture);
        changeListOfItems(data);
        editor.setPicsInAdapter(new ArrayList<>(data));
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPicture;
        LinearLayout llPicContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            llPicContainer = itemView.findViewById(R.id.llPicContainer);

        }
    }

    /**
     * Обновляет отображаемые данные
     *
     * @param items List<P>
     */
    public void changeListOfItems(List items) {
        data = new ArrayList<Pic>(items);
        notifyDataSetChanged();
    }
}
