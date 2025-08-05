package ru.wert.tubus_mobile.data.interfaces;

/**
 * Интерфейс представляет расширение классу Item
 * Если Item, это по-простому ЭЛЕМЕНТ, то TreeBuildingItem - ЭЛЕМЕНТ В ДРЕВОВИДНОЙ СТРУКТУРЕ,
 * то есть элемент, участвующий в построении дерева
 * Интерфейс необходим для описания древовидных структур, т.н. каталогов
 */
public interface TreeBuildingItem extends Item {

    Long getParentId();

    void setParentId(Long parentId);

}
