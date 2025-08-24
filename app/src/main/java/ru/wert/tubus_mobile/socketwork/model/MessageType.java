package ru.wert.tubus_mobile.socketwork.model;

/**
 * Типы сообщений для обмена с сервером
 */
public enum MessageType {
    HEARTBEAT,
    USER_IN,
    USER_OUT,
    UPDATE_PASSPORT,
    ADD_DRAFT,
    UPDATE_DRAFT,
    DELETE_DRAFT,
    ADD_FOLDER,
    UPDATE_FOLDER,
    DELETE_FOLDER,
    DELETE_MESSAGE,
    UPDATE_MESSAGE,
    MESSAGE_DELIVERED,
    CHAT_UPDATE_TEMP_ID
}