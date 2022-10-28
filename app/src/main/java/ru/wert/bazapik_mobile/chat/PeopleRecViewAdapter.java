package ru.wert.bazapik_mobile.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;

public class PeopleRecViewAdapter extends RecyclerView.Adapter<PeopleRecViewAdapter.ViewHolder> {

    private final List<User> data;
    private final LayoutInflater inflater;
    private PeopleClickListener clickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ChatPeopleFragment fragment;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public PeopleRecViewAdapter(ChatPeopleFragment fragment, Context context, List<User> items) {
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
        View view = inflater.inflate(R.layout.recview_people_row, parent, false);
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

        User user = (User) data.get(position);
        //Наименование
        holder.mName.setText(user.getName());
        holder.mName.setTextColor(Color.YELLOW);

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

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.user_name);
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
     * Возвращает User в позиции клика int
     * @param index int
     * @return P extends User
     */
    public User getItem(int index) {
        return (User) data.get(index);
    }

    public void setClickListener(PeopleClickListener peopleClickListener) {
        this.clickListener = peopleClickListener;
    }

    public interface PeopleClickListener {
        void onItemClick(View view, int position);
    }

}

