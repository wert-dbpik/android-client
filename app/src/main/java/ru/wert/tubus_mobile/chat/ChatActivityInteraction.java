package ru.wert.tubus_mobile.chat;

import android.content.Context;

import ru.wert.tubus_mobile.data.models.Room;

public interface ChatActivityInteraction {

    /**
     * Метод возвращает Контекст
     */
    Context getChatContext();

    /**
     * Метод возвращает название комнаты (чата)
     * @param roomNameDB String, название комнаты в БД (one-to-one#1#103)
     */
    String getRoomName(String roomNameDB);

    /**
     * Метод ищет комнату по ее наименованию
     */
    Room findRoomByName(String name);

    /**
     * Метод открывает необходимую комнату
     * @param room Room
     */
    void openRoom(Room room);

    /**
     * Открыть фрагмент КОЛЛЕГИ
     */
    void openPeopleFragment();

    /**
     * Открыть фрагмент МОИ БЕСЕДЫ
     */
    void openRoomsFragment();

}
