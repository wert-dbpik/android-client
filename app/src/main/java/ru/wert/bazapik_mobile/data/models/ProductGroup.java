package ru.wert.bazapik_mobile.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.interfaces.TreeBuildingItem;

/**
 * Класс описывает группы продуктов - элементы каталога изделий,
 * например группа ШКМ в каталоге содержит изделия ШКМ-У1000, ШКМ-У1500А и т.д.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class ProductGroup extends _BaseEntity implements TreeBuildingItem, Serializable {

    private String name;
    private Long parentId;

    @Override
    public String toUsefulString() {
        return name;
    }

    /**
     * Конструктор для создания root в дереве
     * @param id
     * @param name
     */
    public ProductGroup(Long id, Long parentId, String name) {
        super.setId(id);
        this.name = name;
    }


}
