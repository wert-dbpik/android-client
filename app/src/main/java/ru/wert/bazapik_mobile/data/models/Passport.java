package ru.wert.bazapik_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

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
@EqualsAndHashCode(of = {"prefix", "number", "name"}, callSuper = false)
public class Passport extends _BaseEntity implements Item, Parcelable {

    private Prefix prefix;
    private String number;
    private String name;
    private List<Long> draftIds;

    @Override
    public String toUsefulString() {
        String body = number;
        if(prefix.getName().equals("-"))
            return body + ", " + name;
        else
            return prefix.getName() + "." + body + ", " + name;
    }

    public String getNumberWithPrefix(){
        String body = number;
        if(prefix.getName().equals("-"))
            return body;
        else
            return prefix.getName() + "." + body ;
    }

    //==========  Parcelable =======================

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeSerializable(this.prefix);
        dest.writeString(this.number);
        dest.writeString(this.name);
        dest.writeList(this.draftIds);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.prefix = (Prefix) source.readSerializable();
        this.number = source.readString();
        this.name = source.readString();
        this.draftIds = new ArrayList<Long>();
        source.readList(this.draftIds, Long.class.getClassLoader());
    }

    protected Passport(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.prefix = (Prefix) in.readSerializable();
        this.number = in.readString();
        this.name = in.readString();
        this.draftIds = new ArrayList<Long>();
        in.readList(this.draftIds, Long.class.getClassLoader());
    }

    public static final Creator<Passport> CREATOR = new Creator<Passport>() {
        @Override
        public Passport createFromParcel(Parcel source) {
            return new Passport(source);
        }

        @Override
        public Passport[] newArray(int size) {
            return new Passport[size];
        }
    };
}
