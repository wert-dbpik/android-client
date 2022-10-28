package ru.wert.bazapik_mobile.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Message;

public class DialogRecViewAdapter extends RecyclerView.Adapter<DialogRecViewAdapter.ViewHolder> {

    @Getter private final List<Message> data;
    private final LayoutInflater inflater;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ChatDialogFragment fragment;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public DialogRecViewAdapter(ChatDialogFragment fragment, Context context, List<Message> items) {
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
        View view = inflater.inflate(R.layout.recview_message_row, parent, false);
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

        Message message = (Message) data.get(position);
        //Наименование
        holder.sender.setText(message.getSender().getName());
        holder.date.setText(ThisApplication.parseStringToDate(message.getCreationTime()));
        holder.time.setText(ThisApplication.parseStringToTime(message.getCreationTime()));




        new ChatCards().create(context, holder.llMessage, message.getText());

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
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView sender;
        TextView date;
        LinearLayout llMessage;
        TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.tvMessageSender);
            date = itemView.findViewById(R.id.tvMessageDate);
            llMessage = itemView.findViewById(R.id.llMessage);
            time = itemView.findViewById(R.id.tvMessageTime);
        }

    }

    /**
     * Возвращает Message в позиции клика int
     * @param index int
     * @return P extends Message
     */
    public Message getItem(int index) {
        return (Message) data.get(index);
    }

}

