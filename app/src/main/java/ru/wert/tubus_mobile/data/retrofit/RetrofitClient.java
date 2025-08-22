package ru.wert.tubus_mobile.data.retrofit;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.java.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ru.wert.tubus_mobile.ThisApplication.getProp;

import java.util.concurrent.TimeUnit;

/**
 * Клиент для работы с Retrofit, обеспечивающий сетевые запросы к API.
 * Реализован как синглтон для обеспечения единственного экземпляра throughout приложения.
 *
 * Основные функции:
 * - Создание и конфигурация Retrofit экземпляра
 * - Управление базовым URL API
 * - Настройка таймаутов и повторных попыток соединения
 * - Логирование сетевых запросов и ответов
 * - Обработка пустых тел ответов
 */
@Log
public class RetrofitClient extends Application {

    // Таймауты соединения в секундах
    private static final long CONNECT_TIMEOUT = 30;
    private static final long READ_TIMEOUT = 30;
    private static final long WRITE_TIMEOUT = 30;

    // Значения по умолчанию для IP и порта
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String DEFAULT_PORT = "8080";

    // Базовый URL API, доступный статически для других классов
    public static String BASE_URL = "";

    // Экземпляр синглтона
    private static volatile RetrofitClient instance;

    // Retrofit экземпляр для сетевых операций
    private Retrofit retrofit;

    // Gson парсер для JSON преобразований
    private final Gson gson;

    /**
     * Приватный конструктор для реализации паттерна синглтон.
     * Инициализирует Gson, OkHttpClient и Retrofit.
     * Вычисляет базовый URL на основе свойств приложения.
     */
    private RetrofitClient() {
        gson = createGson();

        // Вычисляем базовый URL при инициализации
        BASE_URL = calculateBaseUrl();

        retrofit = buildRetrofit(createOkHttpClient());

        log.info("RetrofitClient инициализирован с BASE_URL: " + BASE_URL);
    }

    /**
     * Возвращает единственный экземпляр RetrofitClient.
     * Реализован с двойной проверкой блокировки для потокобезопасности.
     *
     * @return экземпляр RetrofitClient
     */
    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }

    /**
     * Создает и настраивает экземпляр Gson с ленивым парсингом.
     *
     * @return настроенный экземпляр Gson
     */
    private Gson createGson() {
        return new GsonBuilder()
                .setLenient() // Разрешает ленивый парсинг JSON
                .create();
    }

    /**
     * Создает и настраивает OkHttpClient для сетевых запросов.
     * Добавляет логирование, таймауты и повторные попытки соединения.
     *
     * @return настроенный экземпляр OkHttpClient
     */
    private OkHttpClient createOkHttpClient() {
        // Перехватчик для логирования тел запросов и ответов
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(interceptor) // Добавляет логирование
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) // Таймаут соединения
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) // Таймаут чтения
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) // Таймаут записи
                .retryOnConnectionFailure(true) // Повторные попытки при сбое соединения
                .build();
    }

    /**
     * Строит Retrofit экземпляр с указанным OkHttpClient.
     * Использует предварительно вычисленный BASE_URL.
     *
     * @param okHttpClient настроенный клиент HTTP
     * @return экземпляр Retrofit
     */
    private Retrofit buildRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL) // Базовый URL API
                .addConverterFactory(new NullOnEmptyConverterFactory()) // Обработка пустых тел ответов
                .addConverterFactory(GsonConverterFactory.create(gson)) // Конвертер JSON
                .client(okHttpClient) // HTTP клиент с логированием
                .build();
    }

    /**
     * Вычисляет базовый URL для API на основе свойств приложения.
     * Если свойства не найдены или пусты, используются значения по умолчанию.
     *
     * @return базовый URL в формате "http://ip:port/"
     */
    private String calculateBaseUrl() {
        try {
            // Получаем IP и порт из свойств приложения
            String ip = getProp("IP");
            String port = getProp("PORT");

            // Проверяем и используем значения по умолчанию при необходимости
            if (ip == null || ip.trim().isEmpty()) {
                ip = DEFAULT_IP;
                log.info("Используется IP по умолчанию: " + DEFAULT_IP);
            }
            if (port == null || port.trim().isEmpty()) {
                port = DEFAULT_PORT;
                log.info("Используется порт по умолчанию: " + DEFAULT_PORT);
            }

            String calculatedUrl = "http://" + ip.trim() + ":" + port.trim() + "/";
            log.info("Вычисленный базовый URL: " + calculatedUrl);
            return calculatedUrl;

        } catch (Exception e) {
            log.warning("Ошибка при получении базового URL из свойств: " + e.getMessage());
            String defaultUrl = "http://" + DEFAULT_IP + ":" + DEFAULT_PORT + "/";
            log.info("Используется URL по умолчанию: " + defaultUrl);
            return defaultUrl;
        }
    }

    /**
     * Обновляет базовый URL и пересоздает Retrofit экземпляр.
     * Используется при изменении настроек подключения к серверу.
     *
     * @param newBaseUrl новый базовый URL
     * @throws IllegalArgumentException если newBaseUrl null или пустой
     */
    public static void setBASE_URL(String newBaseUrl) {
        if (newBaseUrl == null || newBaseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Базовый URL не может быть null или пустым");
        }

        // Обновляем только если URL действительно изменился
        if (!newBaseUrl.equals(BASE_URL)) {
            synchronized (RetrofitClient.class) {
                if (!newBaseUrl.equals(BASE_URL)) {
                    BASE_URL = newBaseUrl;

                    // Пересоздаем экземпляр Retrofit с новым URL
                    if (instance != null) {
                        instance.recreateRetrofit();
                        log.info("Базовый URL обновлен: " + BASE_URL);
                    }
                }
            }
        }
    }

    /**
     * Пересоздает Retrofit экземпляр с текущим базовым URL.
     * Используется при динамическом изменении настроек подключения.
     */
    private void recreateRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(createOkHttpClient())
                .build();

        log.info("Retrofit экземпляр пересоздан с URL: " + BASE_URL);
    }

    /**
     * Возвращает текущий экземпляр Retrofit.
     * Используется для получения готового сконфигурированного экземпляра.
     *
     * @return экземпляр Retrofit
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * Создает сервис для работы с API на основе интерфейса.
     * Упрощенный метод для создания API сервисов.
     *
     * @param <T> тип сервиса
     * @param serviceClass класс интерфейса сервиса
     * @return реализация сервиса
     */
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    /**
     * Возвращает текущий базовый URL API.
     * Альтернативный способ получения BASE_URL для классов,
     * которые не могут импортировать статическую переменную.
     *
     * @return текущий базовый URL
     */
    public String getBaseUrl() {
        return BASE_URL;
    }
}



