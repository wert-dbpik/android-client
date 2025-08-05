package ru.wert.tubus_mobile.info;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.data.enums.EDraftStatus;
import ru.wert.tubus_mobile.data.enums.EDraftType;
import ru.wert.tubus_mobile.data.models.Draft;

public class InfoDraftsViewAdapter extends RecyclerView.Adapter<InfoDraftsViewAdapter.ViewHolder>{

    private final List<Draft> mData;
    private final LayoutInflater mInflater;
    private InfoDraftClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public InfoDraftsViewAdapter(Context context, List<Draft> items) {
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
     * Связывает данные чертежа с ViewHolder для указанной позиции в списке.
     * Обрабатывает выделение элемента, отображение типа чертежа и его статуса.
     *
     * @param holder ViewHolder, который должен быть обновлен
     * @param position Позиция элемента в списке данных
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Проверка на null для holder и context
        if (holder == null || holder.itemView == null || context == null) {
            return;
        }

        // Обработка выделения элемента
        View selectedLinearLayout = holder.itemView.findViewById(R.id.selectedLinearLayout);
        if (selectedLinearLayout != null) {
            int highlightColor = ContextCompat.getColor(context, R.color.colorPrimary);
            int backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
            selectedLinearLayout.setBackgroundColor(position == selectedPosition ? highlightColor : backgroundColor);
        }

        // Проверка наличия данных и корректности позиции
        if (mData == null || position < 0 || position >= mData.size()) {
            return;
        }

        Draft item = mData.get(position);
        if (item == null) {
            return;
        }

        // Обработка типа чертежа
        try {
            EDraftType draftType = EDraftType.getDraftTypeById(item.getDraftType());
            String draftText = (draftType != null ? draftType.getTypeName() : "Неизвестный тип") +
                    " - " + (item.getPageNumber() != null ? item.getPageNumber() : "");
            holder.tvDraft.setText(draftText);
        } catch (Exception e) {
            holder.tvDraft.setText("Ошибка типа чертежа");
        }

        // Обработка статуса чертежа
        try {
            EDraftStatus status = EDraftStatus.getStatusById(item.getStatus());
            if (status != null && holder.tvStatus != null) {
                holder.tvStatus.setText(status.getStatusName());

                // Установка цвета для особых статусов
                if (status.equals(EDraftStatus.CHANGED) || status.equals(EDraftStatus.ANNULLED)) {
                    int errorColor = ContextCompat.getColor(context, R.color.colorMyRed);
                    holder.tvStatus.setTextColor(errorColor);
                } else {
                    // Возврат к цвету по умолчанию
                    int defaultColor = ContextCompat.getColor(context, android.R.color.primary_text_dark);
                    holder.tvStatus.setTextColor(defaultColor);
                }
            }
        } catch (Exception e) {
            if (holder.tvStatus != null) {
                holder.tvStatus.setText("Ошибка статуса");
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
            view.findViewById(R.id.selectedLinearLayout)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (mClickListener != null)
                mClickListener.onDraftRowClick(view, getBindingAdapterPosition());

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

    public void setClickListener(InfoDraftClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface InfoDraftClickListener {
        void onDraftRowClick(View view, int position);
    }
}

