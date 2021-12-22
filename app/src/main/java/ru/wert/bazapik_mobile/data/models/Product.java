package ru.wert.bazapik_mobile.data.models;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.interfaces.CatalogableItem;
import ru.wert.bazapik_mobile.data.interfaces.TreeBuildingItem;
import ru.wert.bazapik_mobile.data.util.BLConst;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"passport"}, callSuper = false)
public class Product extends _BaseEntity implements CatalogableItem, Comparable<Product>, Serializable {

    private AnyPart anyPart;
    private ProductGroup productGroup; // папки в каталоге
    private Passport passport;
    private String variant;
    private Folder folder; //Реальная папка в архиве
    private String note;

    @Override
    public TreeBuildingItem getCatalogGroup() {
        return productGroup;
    }

    @Override
    public void setCatalogGroup(TreeBuildingItem treeBuildingItem) {
        this.productGroup = (ProductGroup) treeBuildingItem;
    }

    @Override
    public int compareTo(Product o) {
        if(o.getPassport().getName().equals(BLConst.RAZNOE)) return 0;
        return toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }

    @Override
    public String getName() {
        return passport.getName();
    }

    @Override
    public String toUsefulString() {
        if (super.id == 1L) return BLConst.RAZNOE;
        return passport.toUsefulString();
    }




}
