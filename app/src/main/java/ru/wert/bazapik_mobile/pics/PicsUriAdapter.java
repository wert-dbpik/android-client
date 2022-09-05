package ru.wert.bazapik_mobile.pics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.remark.RemarksEditorActivity;
import ru.wert.bazapik_mobile.viewer.PicsViewerActivity;

import static ru.wert.bazapik_mobile.viewer.PicsViewerActivity.CURRENT_URI;
import static ru.wert.bazapik_mobile.viewer.PicsViewerActivity.ZHABA;

public class PicsUriAdapter extends RecyclerView.Adapter<PicsUriAdapter.ViewHolder> {

    private final String TAG = "PicsAdapter";
    private final LayoutInflater inflater;
    private List<RemarkImage> data;
    private final Context context;
    private final Activity activity;

    private int whoCallMe;
    public static final int INFO_ACTIVITY = 0;
    public static final int REMARK_EDITOR = 1;

    private RemarksEditorActivity editor;

    public PicsUriAdapter(Context context, List<RemarkImage> data, int whoCallMe, RemarksEditorActivity editor) {
        this.context = context;
        this.data = new ArrayList<>(data);
        this.whoCallMe = whoCallMe;
        this.editor = editor;

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
        RemarkImage image = data.get(position);
        Uri uri = image.getUri();
        new Thread(() -> {
            try {
                Bitmap bmp = Picasso.get().load(uri).get();
                activity.runOnUiThread(()->{
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    float w = bmp.getWidth();
                    float h = bmp.getHeight();
                    if (w - h < w * 0.1f)
                        lParams.weight = 0.6f;
                    else if (w - h > w * 0.1f)
                        lParams.weight = 0.9f;
                    else
                        lParams.weight = 0.75f;

                    holder.ivPicture.setLayoutParams(lParams);
                    holder.ivPicture.setImageBitmap(bmp);
                    holder.ivPicture.setAdjustViewBounds(true);
                    holder.ivPicture.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PicsViewerActivity.class);
                        intent.putExtra(ZHABA, "editor");
                        intent.putExtra(CURRENT_URI, new ArrayList(Collections.singleton(uri)));
                        context.startActivity(intent);
                    });
                    if (whoCallMe == REMARK_EDITOR)
                        holder.ivPicture.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                            PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT, R.attr.actionOverflowMenuStyle, 0);
                            popup.getMenuInflater().inflate(R.menu.picture_context_menu, popup.getMenu());
                            popup.setOnMenuItemClickListener(item1 -> {
                                RemarkImage picture = (RemarkImage) data.get(position);
                                switch (item1.getItemId()) {
                                    case R.id.deletePicture:
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
                return;
            }
        }).start();

    }

    private void deletePicture(RemarkImage picture, int position){
        data.remove(picture);
        changeListOfItems(data);
        editor.setImagesInAdapter(new ArrayList<>(data));
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
        data = new ArrayList<RemarkImage>(items);
        notifyDataSetChanged();
    }

}
