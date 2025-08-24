package ru.wert.tubus_mobile.socketwork;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import ru.wert.tubus_mobile.socketwork.model.Message;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сериализатор/десериализатор сообщений с улучшенной обработкой ошибок
 */
public class MessageSerializer {
    private static final String TAG = "MessageSerializer";
    private static final Gson gson = createGson();

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    /**
     * Сериализует сообщение в JSON строку
     * @param message сообщение для сериализации
     * @return JSON строка или пустая строка при ошибке
     */
    public static String serialize(Message message) {
        try {
            return gson.toJson(message);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка сериализации сообщения: " + e.getMessage());
            return "";
        }
    }

    /**
     * Десериализует JSON строку в объект Message с проверкой валидности
     * @param json JSON строка для десериализации
     * @return объект Message или пустой объект при ошибке
     */
    public static Message deserialize(String json) {
        if (json == null || json.trim().isEmpty()) {
            Log.w(TAG, "Получена пустая или null строка");
            return createEmptyMessage();
        }

        // Проверяем, является ли строка валидным JSON объектом
        if (!isValidJsonObject(json)) {
            Log.w(TAG, "Получена невалидная JSON строка: " + getShortenedText(json, 100));
            return createEmptyMessage();
        }

        try {
            return gson.fromJson(json, Message.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Ошибка синтаксиса JSON: " + e.getMessage() +
                    "\nПолученные данные: " + getShortenedText(json, 200));
            return createEmptyMessage();
        } catch (Exception e) {
            Log.e(TAG, "Неизвестная ошибка десериализации: " + e.getMessage() +
                    "\nПолученные данные: " + getShortenedText(json, 200));
            return createEmptyMessage();
        }
    }

    /**
     * Проверяет, является ли строка валидным JSON объектом
     * @param json строка для проверки
     * @return true если строка начинается с { и заканчивается }
     */
    public static boolean isValidJsonObject(String json) {
        if (json == null) return false;

        String trimmed = json.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    /**
     * Создает пустое сообщение с типом UNKNOWN для обработки ошибок
     * @return пустое сообщение
     */
    private static Message createEmptyMessage() {
        Message message = new Message();
        message.setText("invalid_message");
        return message;
    }

    /**
     * Укорачивает текст для логгирования
     * @param text исходный текст
     * @param maxLength максимальная длина
     * @return укороченный текст
     */
    private static String getShortenedText(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>,
            com.google.gson.JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, Type typeOfSrc,
                                                     com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, Type typeOfT,
                                         com.google.gson.JsonDeserializationContext context)
                throws com.google.gson.JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}