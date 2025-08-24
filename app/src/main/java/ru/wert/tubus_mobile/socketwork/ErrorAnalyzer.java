package ru.wert.tubus_mobile.socketwork;

import android.util.Log;

/**
 * Утилитный класс для анализа сетевых ошибок
 */
public class ErrorAnalyzer {
    private static final String TAG = "ErrorAnalyzer";

    /**
     * Анализирует сырые данные и определяет тип ошибки
     * @param rawData сырые данные от сервера
     * @return описание ошибки
     */
    public static String analyzeError(String rawData) {
        if (rawData == null) {
            return "Пустой ответ от сервера";
        }

        if (rawData.contains("HTTP/")) {
            return extractHttpError(rawData);
        }

        if (rawData.contains("<!DOCTYPE html>") || rawData.contains("<html>")) {
            return "Получена HTML страница вместо JSON (возможно, неправильный порт)";
        }

        if (rawData.trim().isEmpty()) {
            return "Пустой ответ от сервера";
        }

        return "Неизвестный формат ответа: " + getShortenedText(rawData, 100);
    }

    private static String extractHttpError(String httpResponse) {
        if (httpResponse.contains("400 Bad Request")) {
            return "HTTP 400 - Неправильный запрос (Bad Request)";
        }
        if (httpResponse.contains("401 Unauthorized")) {
            return "HTTP 401 - Не авторизован";
        }
        if (httpResponse.contains("403 Forbidden")) {
            return "HTTP 403 - Доступ запрещен";
        }
        if (httpResponse.contains("404 Not Found")) {
            return "HTTP 404 - Ресурс не найден";
        }
        if (httpResponse.contains("500 Internal Server Error")) {
            return "HTTP 500 - Внутренняя ошибка сервера";
        }

        return "HTTP ошибка: " + getFirstLine(httpResponse);
    }

    private static String getFirstLine(String text) {
        if (text == null) return "";
        int newlineIndex = text.indexOf('\n');
        if (newlineIndex > 0) {
            return text.substring(0, newlineIndex).trim();
        }
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private static String getShortenedText(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
