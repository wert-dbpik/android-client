package ru.wert.bazapik_mobile.chat;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
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

        View llSelectedContainer = holder.itemView.findViewById(R.id.llSelectedContainer);
        llSelectedContainer.setBackgroundColor((position == selectedPosition) ?
                context.getColor(R.color.colorPrimary) : //Цвет выделения
                context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Message message = (Message) data.get(position);
        //Наименование
        holder.sender.setText(message.getSender().getName());
        holder.date.setText(ThisApplication.parseStringToDate(message.getCreationTime()));
        holder.time.setText(ThisApplication.parseStringToTime(message.getCreationTime()));

        switch(message.getType()){
            case CHAT_SERVICE:
                ChatCards.createServiceCard(context, holder.llMessageContainer, message.getText());
                break;
            case CHAT_TEXT:
                ChatCards.createTextCard(context, holder.llMessageContainer, message.getText());
                break;
            case CHAT_PICS:
                ChatCards.createPicsCard(context, holder.llMessageContainer, message.getText());
                break;
            case CHAT_DRAFTS:
                ChatCards.createDraftsCard(context, holder.llMessageContainer, message.getText());
                break;
            case CHAT_FOLDERS:
                ChatCards.createFoldersCard(context, holder.llMessageContainer, message.getText());
                break;
            case CHAT_PASSPORTS:
                ChatCards.createPassportsCard(context, holder.llMessageContainer, message.getText());
                break;
        }

        if(message.getSender().getId().equals(CURRENT_USER.getId()))
            ChatCards.useMessageOUT_Style(context,
                    holder.llMainContainer, holder.llSelectedContainer, holder.llFitContainer,
                    holder.sender, holder.date, holder.llMessageContainer, holder.time);
        else
            ChatCards.useMessageIN_Style(context,
                    holder.llMainContainer, holder.llSelectedContainer, holder.llFitContainer,
                    holder.sender, holder.date, holder.llMessageContainer, holder.time);
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

        //  MainContainer ->
        // {SelectedContainer ->
        // {FitContainer ->
        // {sender, date, MessageContainer ->
        //          {MESSAGE},
        // time}}}


        LinearLayout llMainContainer;
        LinearLayout llSelectedContainer;
        LinearLayout llFitContainer;

        TextView sender;
        TextView date;
        LinearLayout llMessageContainer;
        TextView time;

        ViewHolder(View itemView) {
            super(itemView);

            llMainContainer = itemView.findViewById(R.id.llMainContainer);
            llSelectedContainer = itemView.findViewById(R.id.llSelectedContainer);
            llFitContainer = itemView.findViewById(R.id.llFitContainer);

            sender = itemView.findViewById(R.id.tvSender);
            date = itemView.findViewById(R.id.tvDate);
            llMessageContainer = itemView.findViewById(R.id.llMessageContainer);
            time = itemView.findViewById(R.id.tvTime);
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

