package ru.wert.bazapik_mobile.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;

/**
 * Класс создает временную папку,
 * куда будут скачивать открываемые файлы
 */
public class TempDirectory extends Application {
    /**
     * TAG класса для логирования
     */
    public static String TAG = "TempDirectory";

    /**
     * instance класса
     */
    private static TempDirectory instance;

    /**
     * Директория временного хранения
     */
    @Getter
    private final File tempDir;

    /**
     * Конструктор
     */
    private TempDirectory(Context context) {
        tempDir = context.getDir("temp", Context.MODE_PRIVATE);
        tempDir.deleteOnExit();
    }

    /**
     * Возвращает instance класса
     * @return TempDirectory
     */
    public static TempDirectory getInstance(Context context){
        if(instance != null)
            return instance;
        instance = new TempDirectory(context);
        return instance;
    }

    /**
     * Загрузка файла при его отсутствии во временной папке
     * @param draft Draft
     * @return boolean, true - успешная загрузка
     */
    public boolean downloadDraft(Draft draft){

        //Если файл уже есть во временной папке
        if(draftInTempDir(draft)) return false;

        //Иначе скачиваем файл во временную папку
        return DraftQuickService.getInstance().download(
                "drafts",
                String.valueOf(draft.getId()), //название скачиваемого файла
                draft.getExtension(), //расширение скачиваемого файла)
                tempDir.getPath());
    }

    /**
     * Проверка наличия файла во временной папке
     * @param draft Draft
     * @return boolean, true - если файл есть в папке
     */
    private boolean draftInTempDir(Draft draft) {
        Long fileId = draft.getId();
        String ext = draft.getExtension();
        String searchedFileName = fileId.toString() + "." + ext;
        Log.d(TAG, String.format("draftInTempDir: проверяем наличие чертежа %s во временной папке", searchedFileName));
        try {
            Path drafts = tempDir.toPath();
            List<Path> filesInFolder = Files.walk(drafts)
                    .filter(Files::isRegularFile).collect(Collectors.toList());

            for(Path p : filesInFolder){
                if(p.getFileName().toString().contains(searchedFileName)) {
                    Log.d(TAG, String.format("draftInTempDir : во временной папке файл %s найден", searchedFileName));
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format("draftInTempDir : во временной папке файл %s НЕ найден", searchedFileName));
        return false;
    }

}
