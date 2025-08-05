package ru.wert.tubus_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.tubus_mobile.data.interfaces.TreeBuildingItem;

/**
 * Класс описывает группы продуктов - элементы каталога изделий,
 * например группа ШКМ в каталоге содержит изделия ШКМ-У1000, ШКМ-У1500А и т.д.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class ProductGroup extends _BaseEntity implements TreeBuildingItem, Serializable, Parcelable {

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

// Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeValue(this.parentId);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.name = source.readString();
        this.parentId = (Long) source.readValue(Long.class.getClassLoader());
    }

    protected ProductGroup(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.parentId = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<ProductGroup> CREATOR = new Creator<ProductGroup>() {
        @Override
        public ProductGroup createFromParcel(Parcel source) {
            return new ProductGroup(source);
        }

        @Override
        public ProductGroup[] newArray(int size) {
            return new ProductGroup[size];
        }
    };
}
