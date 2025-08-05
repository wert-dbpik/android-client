package ru.wert.tubus_mobile.data.interfaces;

import ru.wert.tubus_mobile.data.models.AnyPart;

public interface CatalogableItem extends Item{

    TreeBuildingItem getCatalogGroup();

    void setCatalogGroup(TreeBuildingItem treeBuildingItem);

    AnyPart getAnyPart();

}
