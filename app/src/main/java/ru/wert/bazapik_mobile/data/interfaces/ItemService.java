package ru.wert.bazapik_mobile.data.interfaces;

import java.util.List;

import ru.wert.bazapik_mobile.data.exceptions.ItemIsBusyException;

/**
 *Иинтрефейс описывает группу интерфейсов классов типа Item и ниже
 * @param <T> <T extends Item>
 */
public interface ItemService<T extends Item> {

    T findById(Long id);

    boolean save(T t) throws Exception;

    boolean update(T t) throws Exception;

    boolean delete(T t) throws ItemIsBusyException, Exception;

    List<T> findAll() throws Exception;

    List<T> findAllByText(String text);

}
