package ru.wert.bazapik_mobile.organizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.ProductGroup;

public class FoldersRecViewAdapter extends RecyclerView.Adapter<FoldersRecViewAdapter.ViewHolder> implements OrganizerRecViewAdapter{

    private final List<Item> mData;
    private final LayoutInflater mInflater;
    private ItemFolderClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public FoldersRecViewAdapter(Context context, List<Item> items) {
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
        View view = mInflater.inflate(R.layout.recview_folder_row, parent, false);

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

        Item item = mData.get(position);

        if(item instanceof ProductGroup){
            String str = ((ProductGroup)item).getName();
            holder.numberAndName.setText(str);
            holder.folder.setImageDrawable(context.getDrawable(R.drawable.folder256));
            holder.folder.setOnClickListener(e->{

            });
        }

        if(item instanceof Folder){
            String str = ((Folder)item).getName();
            holder.numberAndName.setText(str);
            holder.folder.setImageDrawable(null);
            holder.folder.setClickable(false);
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
    public void changeListOfItems(List items){
        mData.clear();
        mData.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView numberAndName;
        ImageButton folder; //кнопка в виде чертежика


        ViewHolder(View itemView) {
            super(itemView);
            numberAndName = itemView.findViewById(R.id.number_and_name);
            folder = itemView.findViewById(R.id.btnFolder);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getAdapterPosition();
            view.findViewById(R.id.selected_position)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Item getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemFolderClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemFolderClickListener {
        void onItemClick(View view, int position);
    }

}

