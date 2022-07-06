package ru.wert.bazapik_mobile.info;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.enums.EDraftType;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Remark;

public class InfoRemarksViewAdapter extends RecyclerView.Adapter<InfoRemarksViewAdapter.ViewHolder>{

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
    public InfoRemarksViewAdapter(Context context, List<Remark> items) {
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

        View selectedLinearLayout = holder.itemView.findViewById(R.id.selectedLinearLayout);
        selectedLinearLayout.setBackgroundColor((position == selectedPosition) ?
                context.getColor(R.color.colorPrimary) : //Цвет выделения
                context.getColor(R.color.colorPrimaryDark)); //Цвет фона


        Remark item = mData.get(position);
        holder.tvRemarkUser.setText(item.getUser().getName());
        holder.tvRemarkText.setText(item.getText());
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
        TextView tvRemarkText;

        ViewHolder(View itemView) {
            super(itemView);
            tvRemarkUser = itemView.findViewById(R.id.tvRemarkUser);
            tvRemarkText = itemView.findViewById(R.id.tvRemarkText);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selectedLinearLayout)
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
    public Remark getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(InfoRemarkClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface InfoRemarkClickListener {
        void onItemClick(View view, int position);
    }
}

