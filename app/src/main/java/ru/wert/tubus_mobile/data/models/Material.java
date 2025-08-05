package ru.wert.tubus_mobile.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.tubus_mobile.data.interfaces.CatalogableItem;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.interfaces.TreeBuildingItem;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class Material extends _BaseEntity implements Item, CatalogableItem, Serializable {

    private AnyPart anyPart;
    private MaterialGroup catalogGroup; // папки в каталоге
    String name;
    MatType matType;
    String note;
    double paramS; //толщина (t), диаметр (D), периметр P
    double paramX;//плотность, масса пог. м. (Mпог.м)


    //Конструктор необходим для создания узлов дерева
    public Material(String name) {
        super.setId(0L);
        this.anyPart = null;
        this.catalogGroup = null;
        this.name = name;
        this.matType = null;
        this.note = "";
        this.paramS = 0;
        this.paramX = 0;
    }

    @Override
    public void setCatalogGroup(TreeBuildingItem treeBuildingItem) {
        this.catalogGroup = (MaterialGroup) treeBuildingItem;
    }

    @Override
    public String toUsefulString() {
        return name;
    }


}
