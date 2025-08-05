package ru.wert.tubus_mobile.remark;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;
import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.data.models.Pic;
import ru.wert.tubus_mobile.data.models.Remark;
import ru.wert.tubus_mobile.info.InfoActivity;
import ru.wert.tubus_mobile.viewer.PicsViewerActivity;
import ru.wert.tubus_mobile.viewer.ViewerActivity;

import static ru.wert.tubus_mobile.data.retrofit.RetrofitClient.BASE_URL;
import static ru.wert.tubus_mobile.viewer.PicsViewerActivity.$ALL_PICS;
import static ru.wert.tubus_mobile.viewer.PicsViewerActivity.$CURRENT_PIC;

public class RemarksAdapter extends RecyclerView.Adapter<RemarksAdapter.ViewHolder> {


    @Getter private final List<Remark> data;
    private final LayoutInflater mInflater;
    private InfoRemarkClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    public static String REMARK_POSITION = "remark_position";

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public RemarksAdapter(Context context, List<Remark> items) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = items;

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

        Remark remark = data.get(position);
        holder.tvRemarkUser.setText(remark.getUser().getName());
        holder.tvRemarkTime.setText(ThisApplication.parseStringToDate(remark.getCreationTime()));
        holder.tvRemarkText.setText(remark.getText());

        holder.btnShowRemarkMenu.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.remark_context_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.putRemarkInTheTop:
                        ((InfoActivity)context).putRemarkInTheTop(remark, position);
                        break;
                    case R.id.changeRemark:
                        ((InfoActivity)context).openChangeRemarkActivity(remark, position);
                        break;
                    case R.id.deleteRemark:
                        ((InfoActivity)context).deleteRemark(remark, position);
                        break;
                }
                return true;
            });
            popup.show();
        });

        holder.llRemark.removeAllViews();

        List<Pic> picsInRemark = new ArrayList<>();
        if(remark.getPicsInRemark() != null && !remark.getPicsInRemark().isEmpty()){
            picsInRemark = new ArrayList<>(remark.getPicsInRemark());
            for(Pic pic: picsInRemark) {
                addPictureToRemark(holder, pic, picsInRemark);

            }
        }

        holder.llRecViewRemark.setBackgroundColor(context.getColor(R.color.colorPrimaryDark));

        if(context instanceof ViewerActivity){
            holder.llRecViewRemark.setBackgroundColor(context.getColor(R.color.colorMyDarkerGray));
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

    private void addPictureToRemark(ViewHolder holder, Pic pic, List<Pic> picsInRemark) {
        String str = BASE_URL + "files/download/pics/" + pic.getId() + "." + pic.getExtension();
        Uri uri = Uri.parse(str);
        LinearLayout.LayoutParams lParamsLL  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(lParamsLL);
        ll.setWeightSum(1.0f);
        LinearLayout.LayoutParams lParams  = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        int w = pic.getWidth();
        int h = pic.getHeight();

        float weight;
        if (w - h < w * 0.1f)
            weight = 0.6f;
        else if (w - h > w * 0.1f)
            weight = 0.9f;
        else
            weight = 0.7f;
        lParams.weight = weight;
        lParams.gravity = Gravity.START;

        ImageView imageView = new ImageView(context);
        Picasso.get().load(uri).into(imageView);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(0, 20, 0, 20);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PicsViewerActivity.class);
            intent.putParcelableArrayListExtra($ALL_PICS, (ArrayList<? extends Parcelable>) picsInRemark);
            intent.putExtra($CURRENT_PIC, pic);
            context.startActivity(intent);
        });

        ll.addView(imageView, lParams);
        holder.llRemark.addView(ll);
    }

    /**
     * Возвращает общее количество элементов в списке List<P>
     * @return int
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Обновляет отображаемые данные с заданным списком
     * @param items List<P>
     */
    public void changeListOfItems(List<Remark> items){
        data.clear();
        data.addAll(items);
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
        LinearLayout llRecViewRemark;
        RecyclerView rvRemarkPics;
        ImageButton btnShowRemarkMenu;
        LinearLayout iv;
        LinearLayout llRemark;


        ViewHolder(View itemView) {
            super(itemView);
            tvRemarkUser = itemView.findViewById(R.id.tvRemarkUser);
            tvRemarkTime = itemView.findViewById(R.id.tvRemarkTime);
            tvRemarkText = itemView.findViewById(R.id.tvRemarkText);
            iv = itemView.findViewById(R.id.selectedLinearLayout);
//            rvRemarkPics = itemView.findViewById(R.id.rvRemarkPics);
            llRecViewRemark = itemView.findViewById(R.id.llRecViewRemark);
            btnShowRemarkMenu = itemView.findViewById(R.id.btnShowRemarkMenu);
            btnShowRemarkMenu = itemView.findViewById(R.id.btnShowRemarkMenu);
            llRemark = itemView.findViewById(R.id.llRemark);
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
        return data.get(index);
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

