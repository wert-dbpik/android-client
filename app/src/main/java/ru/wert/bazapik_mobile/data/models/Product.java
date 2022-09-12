package ru.wert.bazapik_mobile.data.models;


import android.os.Parcel;
import android.os.Parcelable;

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
public class Product extends _BaseEntity implements CatalogableItem, Comparable<Product>, Serializable, Parcelable {

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

//Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeParcelable(this.anyPart, flags);
        dest.writeParcelable(this.productGroup, flags);
        dest.writeParcelable(this.passport, flags);
        dest.writeString(this.variant);
        dest.writeParcelable(this.folder, flags);
        dest.writeString(this.note);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.anyPart = source.readParcelable(AnyPart.class.getClassLoader());
        this.productGroup = source.readParcelable(ProductGroup.class.getClassLoader());
        this.passport = source.readParcelable(Passport.class.getClassLoader());
        this.variant = source.readString();
        this.folder = source.readParcelable(Folder.class.getClassLoader());
        this.note = source.readString();
    }

    protected Product(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.anyPart = in.readParcelable(AnyPart.class.getClassLoader());
        this.productGroup = in.readParcelable(ProductGroup.class.getClassLoader());
        this.passport = in.readParcelable(Passport.class.getClassLoader());
        this.variant = in.readString();
        this.folder = in.readParcelable(Folder.class.getClassLoader());
        this.note = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
