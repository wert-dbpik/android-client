package ru.wert.bazapik_mobile.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Passport;

public class ItemRecViewAdapter<P extends Item> extends RecyclerView.Adapter<ItemRecViewAdapter<P>.ViewHolder>{

    private final List<P> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final Context context;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public ItemRecViewAdapter(Context context, List<P> items) {
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
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        P item = mData.get(position);
        String text;
        if(item instanceof Passport){
            if(Consts.HIDE_PREFIXES)
                text = ((Passport) item).getNumber();
            else
                text = ((Passport) item).getPrefix().getName() + "." + ((Passport) item).getNumber();
        } else
            text = item.toUsefulString();
        holder.mNumber.setText(text);
        holder.mName.setText(item.getName());

        if(item instanceof Passport){
            holder.mShowDraft.setImageDrawable(
                    ContextCompat.getDrawable(this.mInflater.getContext(), R.drawable.draft));
            if(((Passport) item).getDraftIds().isEmpty())
                holder.mShowDraft.setBackgroundColor(Color.BLACK);
            else {
                holder.mShowDraft.setBackgroundColor(Color.WHITE);
                //При нажатии на кнопку создаем активити ViewerActivity, передаем ArrayList<String>, состоящий из id чертежей пасспорта
                holder.mShowDraft.setOnClickListener(e->{
                    Intent intent = new Intent(context, ViewerActivity.class);
                    ArrayList<String> draftIds = (ArrayList<String>) ((Passport) item).getDraftIds().stream().map(Object::toString)
                            .collect(Collectors.toList());
                    intent.putStringArrayListExtra("draftIds", draftIds);
                    context.startActivity(intent);
                });
            }

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
     * Обновляет отображаемые данные
     * @param items List<P>
     */
    public void changeListOfItems(List<P> items){
        mData.clear();
        mData.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mNumber;
        TextView mName;
        ImageButton mShowDraft; //кнопка в виде чертежика

        ViewHolder(View itemView) {
            super(itemView);
            mNumber = itemView.findViewById(R.id.number);
            mName = itemView.findViewById(R.id.name);
            mShowDraft = itemView.findViewById(R.id.show_draft);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public P getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}

