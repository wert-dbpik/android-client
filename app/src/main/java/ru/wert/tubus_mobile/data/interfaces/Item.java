package ru.wert.tubus_mobile.data.interfaces;

/**
 * Интерфейс содержит основные методы getName(), getId(), toUsefulString()
 * toUsefulString() - имитирует toString() - возвращает строку, которую нужно использовать для вывода
 */
public interface Item{

    String getName();

    Long getId();

    String toUsefulString();

}
