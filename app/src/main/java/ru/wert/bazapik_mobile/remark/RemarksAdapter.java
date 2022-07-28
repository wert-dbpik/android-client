package ru.wert.bazapik_mobile.remark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.pics.PicsAdapter;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;

import static ru.wert.bazapik_mobile.data.retrofit.RetrofitClient.BASE_URL;

public class RemarksAdapter extends RecyclerView.Adapter<RemarksAdapter.ViewHolder>{

    private final List<Remark> mData;
    private final LayoutInflater mInflater;
    private InfoRemarkClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public RemarksAdapter(Context context, List<Remark> items) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = items;
    }

    /**
     * Создает новый view из имеющегося xml файла с помощью метода inflate класса LayoutInflater
     * @param parent ViewGroup
     * @param viewType int
     * @return новый view типа ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recview_remark_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        View llRemark = holder.itemView.findViewById(R.id.llRemark);
        if (context instanceof ViewerActivity) {
            llRemark.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
            holder.btnShowRemarkMenu.setVisibility(View.INVISIBLE);
            holder.btnShowRemarkMenu.setClickable(false);
        } else
            llRemark.setBackgroundColor(context.getColor(R.color.colorPrimaryDark));

        Remark remark = mData.get(position);
        holder.tvRemarkUser.setText(remark.getUser().getName());
        holder.tvRemarkTime.setText(ThisApplication.parseStringToDate(remark.getCreationTime()));
        holder.tvRemarkText.setText(remark.getText());

        holder.btnShowRemarkMenu.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.remark_context_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.changeRemark:
                        ((InfoActivity)context).getRm().openChangeRemarkFragment(remark);
                        break;
                    case R.id.deleteRemark:
                        ((InfoActivity)context).getRm().deleteRemark(remark, position);
                        break;
                }
                return true;
            });
            popup.show();
        });


        List<Pic> picsInRemark =
                remark.getPicsInRemark() == null || remark.getPicsInRemark().isEmpty() ?
                        new ArrayList<>() :
                        remark.getPicsInRemark();

        for(Pic pic: picsInRemark) {
            String str = BASE_URL + "files/download/pics/" + pic.getId() + "." + pic.getExtension();
            Uri uri = Uri.parse(str);
            ImageView imageView = new ImageView(context);
            Picasso.get().load(uri).into(imageView);
            imageView.setPadding(0, 20, 0, 20);
            holder.llPictures.setWeightSum(1f);
            holder.llPictures.addView(imageView);
            LinearLayout.LayoutParams lParams  = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            int w = pic.getWidth();
            int h = pic.getHeight();
            if (w - h < w * 0.1f)
                lParams.weight = 0.6f;
            else if (w - h > w * 0.1f)
                lParams.weight = 0.9f;
            else
                lParams.weight = 0.75f;
            imageView.setLayoutParams(lParams);



        }

//            Picasso.get().load(uri).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bmp, Picasso.LoadedFrom from) {
//                    LinearLayout.LayoutParams lParams  = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    float w = bmp.getWidth();
//                    float h = bmp.getHeight();
//                    if(w - h < w * 0.1f)
//                        lParams.weight = 0.6f;
//                    else if(w - h > w * 0.1f)
//                        lParams.weight = 0.9f;
//                    else
//                        lParams.weight = 0.75f;
//                    imageView.setLayoutParams(lParams);
//                    imageView.setImageBitmap(bmp);
//
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            });


//        holder.rvRemarkPics.setLayoutManager(new LinearLayoutManager(context));
//        PicsAdapter picsAdapter = new PicsAdapter(context, new ArrayList<>(picsInRemark), PicsAdapter.INFO_ACTIVITY);
//        holder.rvRemarkPics.setAdapter(picsAdapter);


        if(context instanceof ViewerActivity){
            holder.tvRemarkUser.setTextColor(context.getColor(R.color.colorWhite));
            holder.tvRemarkUser.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
            holder.tvRemarkTime.setTextColor(context.getColor(R.color.colorWhite));
            holder.tvRemarkTime.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
            holder.tvRemarkText.setTextColor(context.getColor(R.color.colorWhite));
            holder.tvRemarkText.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
            holder.itemView.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
            holder.iv.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
        }
    }


    /**
     * Возвращает общее количество элементов в списке List<P>
     * @return int
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Обновляет отображаемые данные с заданным списком
     * @param items List<P>
     */
    public void changeListOfItems(List<Remark> items){
        mData.clear();
        mData.addAll(items);
        notifyDataSetChanged();
    }


    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvRemarkUser;
        TextView tvRemarkTime;
        TextView tvRemarkText;
        LinearLayout llPictures;
        RecyclerView rvRemarkPics;
        ImageButton btnShowRemarkMenu;
        LinearLayout iv;

        ViewHolder(View itemView) {
            super(itemView);
            tvRemarkUser = itemView.findViewById(R.id.tvRemarkUser);
            tvRemarkTime = itemView.findViewById(R.id.tvRemarkTime);
            tvRemarkText = itemView.findViewById(R.id.tvRemarkText);
            iv = itemView.findViewById(R.id.selectedLinearLayout);
//            rvRemarkPics = itemView.findViewById(R.id.rvRemarkPics);
            llPictures = itemView.findViewById(R.id.llPictures);
            btnShowRemarkMenu = itemView.findViewById(R.id.btnShowRemarkMenu);

//            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selectedLinearLayout)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            notifyDataSetChanged();
        }

    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Remark getItem(int index) {
        return mData.get(index);
    }
//
    public void setClickListener(InfoRemarkClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
//
    public interface InfoRemarkClickListener {
        void onRemarkRowClick(View view, int position);
//        void onRemarkRowLongClick(View view, int position);
    }

}

