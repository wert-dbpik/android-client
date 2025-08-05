package ru.wert.tubus_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.*;
import ru.wert.tubus_mobile.data.interfaces.Item;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class Room extends _BaseEntity implements Item, Parcelable {

    private String name; //заголовок чата, его название
    private User creator; //id пользователя создавшего чат
    private boolean editable; //Можно изменять список пользователей
    private List<User> roommates;


    @Override
    public String toUsefulString() {
        return name;
    }

    //==========  Parcelable =======================


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeParcelable(this.creator, flags);
        dest.writeTypedList(this.roommates);
        dest.writeValue(this.id);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.name = source.readString();
        this.creator = source.readParcelable(User.class.getClassLoader());
        this.roommates = source.createTypedArrayList(User.CREATOR);
        this.id = (Long) source.readValue(Long.class.getClassLoader());
    }

    protected Room(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.creator = in.readParcelable(User.class.getClassLoader());
        this.roommates = in.createTypedArrayList(User.CREATOR);
        this.id = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel source) {
            return new Room(source);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
}
