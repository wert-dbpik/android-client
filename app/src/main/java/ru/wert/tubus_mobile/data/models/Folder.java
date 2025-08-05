package ru.wert.tubus_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

//import javax.xml.soap.Detail;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.util.BLConst;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"productGroup", "name"}, callSuper = false)
public class Folder extends _BaseEntity implements Item, Comparable<Folder>, Serializable, Parcelable {

    private ProductGroup productGroup;
    private String name;
    private String note;

    @Override
    public int compareTo(@NotNull Folder o) {
        if(o.getName().equals(BLConst.RAZLOZHENO))
            return 0;
        return
                toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }


    @Override
    public String toUsefulString() {
        return name;
    }

    // Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeSerializable(this.productGroup);
        dest.writeString(this.name);
        dest.writeString(this.note);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.productGroup = (ProductGroup) source.readSerializable();
        this.name = source.readString();
        this.note = source.readString();
    }

    protected Folder(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.productGroup = (ProductGroup) in.readSerializable();
        this.name = in.readString();
        this.note = in.readString();
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel source) {
            return new Folder(source);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
}
