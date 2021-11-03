package ru.wert.bazapik_mobile.data.interfaces;

import java.util.List;

import ru.wert.bazapik_mobile.data.exceptions.ItemIsBusyException;

/**
 *Иинтрефейс описывает группу интерфейсов классов типа Item и ниже
 * @param <T> <T extends Item>
 */
public interface ItemService<T extends Item> {

    T findById(Long id);

    boolean save(T t);

    boolean update(T t);

    boolean delete(T t) throws ItemIsBusyException;

    List<T> findAll();

    List<T> findAllByText(String text);

}
