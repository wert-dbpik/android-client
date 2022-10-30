package ru.wert.bazapik_mobile.chat;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lombok.Getter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.models.Message;

public class DialogRecViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE_SERVICE = 0;
    private static final int TYPE_MESSAGE_IN = 1;
    private static final int TYPE_MESSAGE_OUT = 2;


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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch(viewType){
            case TYPE_MESSAGE_SERVICE:
                View view = inflater.inflate(R.layout.recview_message_service_row, parent, false);
                return new ViewHolderMessageService(view);
            case TYPE_MESSAGE_IN:
                View view1 = inflater.inflate(R.layout.recview_message_in_row, parent, false);
                return new ViewHolderMessageIN(view1);
            case TYPE_MESSAGE_OUT:
                View view2 = inflater.inflate(R.layout.recview_message_out_row, parent, false);
                return new ViewHolderMessageOUT(view2);
            default: throw new RuntimeException("Type of ViewHolder is out of possible range!");
        }

    }

    // determine which layout to use for the row
    @Override
    public int getItemViewType(int position) {

        Message message = (Message) data.get(position);

        if (message.getType().equals(Message.MessageType.CHAT_SERVICE)) {
            return TYPE_MESSAGE_SERVICE;
        } else if (message.getSender().getId().equals(CURRENT_USER.getId())) {
            return TYPE_MESSAGE_OUT;
        } else {
            return TYPE_MESSAGE_IN;
        }
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE_SERVICE:
                initTypeMessageService((ViewHolderMessageService)holder, position);
                break;
            case TYPE_MESSAGE_IN:
                initTypeMessageIN((ViewHolderMessageIN)holder, position);
                break;
            case TYPE_MESSAGE_OUT:
                initTypeMessageOUT((ViewHolderMessageOUT)holder, position);
                break;
        }

    }

    /**
     * onBindViewHolder для сервисных сообщений
     */
    private void initTypeMessageService(ViewHolderMessageService holder, int position){
        Message message = (Message) data.get(position);
        ChatCards.createServiceCard(context, holder.llMessageContainer, message.getText());
    }

    /**
     * onBindViewHolder для исходящих сообщений
     */
    private void initTypeMessageOUT(ViewHolderMessageOUT holder, int position){
        Message message = (Message) data.get(position);
        createMessageCard(holder.llMessageContainer, message);
    }

    /**
     * onBindViewHolder для входящих сообщений
     */
    private void initTypeMessageIN(ViewHolderMessageIN holder, int position){
        Message message = (Message) data.get(position);
        holder.sender.setText(message.getSender().getName());
        holder.time.setText(ThisApplication.parseStringToTime(message.getCreationTime()));

        createMessageCard(holder.llMessageContainer, message);

    }

    /**
     * Метод определяет, как message будет вложен в llMessageContainer
     */
    private void createMessageCard(LinearLayout llMessageContainer, Message message) {
        switch(message.getType()){
            case CHAT_TEXT:
                ChatCards.createTextCard(context, llMessageContainer, message.getText());
                break;
            case CHAT_PICS:
                ChatCards.createPicsCard(context, llMessageContainer, message.getText());
                break;
            case CHAT_DRAFTS:
                ChatCards.createDraftsCard(context, llMessageContainer, message.getText());
                break;
            case CHAT_FOLDERS:
                ChatCards.createFoldersCard(context, llMessageContainer, message.getText());
                break;
            case CHAT_PASSPORTS:
                ChatCards.createPassportsCard(context, llMessageContainer, message.getText());
                break;
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
     * ViewHolder для всех сервисных сообщений типа даты
     */
    static class ViewHolderMessageService extends RecyclerView.ViewHolder{

        LinearLayout llMessageContainer;

        ViewHolderMessageService(View itemView) {
            super(itemView);

            llMessageContainer = itemView.findViewById(R.id.llMessageContainer);
        }

    }

    /**
     * ViewHolder для всех входящих сообщений
     */
    static class ViewHolderMessageIN extends RecyclerView.ViewHolder{

        TextView sender;
        LinearLayout llMessageContainer;
        TextView time;

        ViewHolderMessageIN(View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.tvSender);
            llMessageContainer = itemView.findViewById(R.id.llMessageContainer);
            time = itemView.findViewById(R.id.tvTime);
        }

    }

    /**
     * ViewHolder для всех исходящих сообщений
     */
    static class ViewHolderMessageOUT extends RecyclerView.ViewHolder{

        LinearLayout llMessageContainer;

        ViewHolderMessageOUT(View itemView) {
            super(itemView);
            llMessageContainer = itemView.findViewById(R.id.llMessageContainer);
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

