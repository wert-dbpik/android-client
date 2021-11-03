package ru.wert.bazapik_mobile.data.interfaces;

import ru.wert.bazapik_mobile.data.models.AnyPart;

public interface CatalogableItem extends Item{

    TreeBuildingItem getCatalogGroup();

    void setCatalogGroup(TreeBuildingItem treeBuildingItem);

    AnyPart getAnyPart();

}
