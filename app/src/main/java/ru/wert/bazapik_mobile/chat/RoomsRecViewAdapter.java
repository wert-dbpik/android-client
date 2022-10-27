package ru.wert.bazapik_mobile.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;

public class RoomsRecViewAdapter extends RecyclerView.Adapter<RoomsRecViewAdapter.ViewHolder> {

    private final List<Room> data;
    private final LayoutInflater inflater;
    private RoomsClickListener clickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ChatRoomsFragment fragment;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public RoomsRecViewAdapter(ChatRoomsFragment fragment, Context context, List<Room> items) {
        this.fragment = fragment;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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
        View view = inflater.inflate(R.layout.recview_room_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        View selectedLinearLayout = holder.itemView.findViewById(R.id.selectedLinearLayout);
        selectedLinearLayout.setBackgroundColor((position == selectedPosition) ?
                context.getColor(R.color.colorPrimary) : //Цвет выделения
                context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Room room = (Room) data.get(position);
        //Наименование
        holder.mName.setText(((ChatActivity)fragment.getActivity()).getRoomName(room.getName()));
        if(room.getName().startsWith("one-to-one")) {
            holder.ivUserImage.setImageDrawable(context.getDrawable(R.drawable.one_of_group));
            holder.mName.setTextColor(Color.YELLOW);
        }else if(room.getName().startsWith("group")) {
            holder.ivUserImage.setImageDrawable(context.getDrawable(R.drawable.group));
            holder.mName.setTextColor(Color.CYAN);
        }
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
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mName;
        ImageView ivUserImage;

        ViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.room_name);
            ivUserImage = itemView.findViewById(R.id.ivUserImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selectedLinearLayout)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (clickListener != null)
                clickListener.onItemClick(view, getBindingAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Room в позиции клика int
     * @param index int
     * @return P extends Room
     */
    public Room getItem(int index) {
        return (Room) data.get(index);
    }

    public void setClickListener(RoomsClickListener roomsClickListener) {
        this.clickListener = roomsClickListener;
    }

    public interface RoomsClickListener {
        void onItemClick(View view, int position);
    }

}

