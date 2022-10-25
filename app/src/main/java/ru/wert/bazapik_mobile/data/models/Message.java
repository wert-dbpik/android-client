package ru.wert.bazapik_mobile.data.models;

import lombok.*;
import ru.wert.bazapik_mobile.data.interfaces.Item;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Message extends _BaseEntity implements Item {

    public enum MessageStatus {
        RECEIVED, //Уведомление получено
        DELIVERED //Сообщение прочитано
    }

    public enum MessageType {
        CHAT_TEXT("текст"),
        CHAT_DRAFTS("чертежи"),
        CHAT_FOLDERS("комплекты чертежей"),
        CHAT_PICS("изображения"),
        CHAT_PASSPORTS("пасспорта");

        @Getter String typeName;

        MessageType(String typeName) {
            this.typeName = typeName;
        }
    }


    private MessageType type; //Тип сообщения (текстовый, чертеж и т.д.)
    private Room room; //id руппы чата
    private User sender; //id пользователя, написавшего в группе
    private String text; //Текст сообщения, либо строку id-шников
    private String creationTime; //Время отправки сообщения
    private MessageStatus status; //Время отправки сообщения

    @Override
    public String getName() {
        // НЕ ИСПОЛЬЗУЕТСЯ
        return null;
    }

    @Override
    public String toUsefulString() {
        return "from: " + sender.getName() + "type: " + type.getTypeName() + " ,message: " + text;
    }
}
