package ru.wert.bazapik_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.interfaces.Item;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class Prefix extends _BaseEntity implements Item, Serializable, Parcelable {

    private String name;
    private String note;

    @Override
    public String toUsefulString() {
        return name;
    }

    //  Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.note);

    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.name = source.readString();
        this.note = source.readString();

    }

    protected Prefix(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.note = in.readString();

    }

    public static final Creator<Prefix> CREATOR = new Creator<Prefix>() {
        @Override
        public Prefix createFromParcel(Parcel source) {
            return new Prefix(source);
        }

        @Override
        public Prefix[] newArray(int size) {
            return new Prefix[size];
        }
    };
}
