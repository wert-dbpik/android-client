package ru.wert.bazapik_mobile.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.interfaces.TreeBuildingItem;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class MaterialGroup extends _BaseEntity implements Item, TreeBuildingItem, Serializable {

    private Long parentId;
    private String name;

    @Override
    public String toUsefulString() {
        return name;
    }

    /**
     * Конструктор для создания root в дереве
     * @param id
     * @param name
     */
    public MaterialGroup(Long id, Long parentId, String name) {
        super.setId(id);
        this.name = name;
    }
}
