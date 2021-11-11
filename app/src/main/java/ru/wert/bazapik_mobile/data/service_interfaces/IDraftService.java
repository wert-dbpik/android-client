package ru.wert.bazapik_mobile.data.service_interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Product;


public interface IDraftService extends ItemService<Draft> {

    List<String> findDraftsByMask(String folder, String mask);

    boolean download(String path, String fileName, String extension, String tempDir);

    boolean upload(String fileName, String path, File draft) throws IOException;

    Set<Product> findProducts(Draft draft);

    /**
     * Добавить изделие к чертежу
     */
    Set<Product> addProducts(Draft draft, Product product);

    /**
     * Удалить изделие, относящееся к чертежу
     */
    Set<Product> removeProducts(Draft draft, Product product);

    /***
     * Искать все чертежи входящие в папку
     */
    Set<Draft> findAllByFolder(Folder folder);


    //==============================================
    List<Draft> findByPassportId(Long id);


}
