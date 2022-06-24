package ru.wert.bazapik_mobile.info;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;

public class PassportRecViewAdapter extends RecyclerView.Adapter<PassportRecViewAdapter.ViewHolder>{

    private final List<Draft> mData;
    private final LayoutInflater mInflater;
    private PassportClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public PassportRecViewAdapter(Context context, List<Draft> items) {
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
        View view = mInflater.inflate(R.layout.recview_draft_info, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (selectedPosition != RecyclerView.NO_POSITION) //Если ничего не выделенно
            holder.itemView.findViewById(R.id.selected_position)
                    .setBackgroundColor((position == selectedPosition) ?
                            context.getColor(R.color.colorPrimary) : //Цвет выделения
                            context.getColor(R.color.colorPrimaryDark)); //Цвет фона


        Draft item = mData.get(position);
        String draftType = EDraftType.getDraftTypeById(item.getDraftType()).getTypeName() + " - " + item.getPageNumber();
        holder.tvDraft.setText(draftType);
        holder.tvStatus.setText(EDraftStatus.getStatusById(item.getStatus()).getStatusName());
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
     * Обновляет отображаемые данные
     * @param items List<P>
     */
    public void changeListOfItems(List<Draft> items){
        mData.clear();
        mData.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvDraft;
        TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvDraft = itemView.findViewById(R.id.tvDraft);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selected_position)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());

            notifyDataSetChanged();
        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Draft getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(PassportClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface PassportClickListener {
        void onItemClick(View view, int position);
    }
}

