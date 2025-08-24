package ru.wert.tubus_mobile.socketwork.model;

import java.time.LocalDateTime;

/**
 * Класс сообщения для обмена с сервером
 * Аналогичен серверному классу Message
 */
public class Message {

    private Long id;
    private String tempId;
    private MessageType type;
    private Long roomId = 1L; // Значение по умолчанию для комнаты
    private Long senderId = 1L; // Значение по умолчанию для отправителя
    private String text = ""; // Пустой текст для heartbeat
    private LocalDateTime creationTime;
    private MessageStatus status = MessageStatus.SENT;

    public Message() {
        this.creationTime = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTempId() { return tempId; }
    public void setTempId(String tempId) { this.tempId = tempId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreationTime() { return creationTime; }
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
}