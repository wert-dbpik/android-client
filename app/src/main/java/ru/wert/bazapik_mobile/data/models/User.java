package ru.wert.bazapik_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

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
public class User extends _BaseEntity implements Item, Parcelable {

    private String name;
    private String password;
    private UserGroup userGroup;
    private boolean logging; //следует ли пользователя логировать
    private boolean active; //активный ли пользователь
    @Override
    public String toUsefulString() {
        return name;
    }

//-------   Parcelable ------------


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.password);
        dest.writeParcelable(this.userGroup, flags);
        dest.writeByte(this.logging ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.name = source.readString();
        this.password = source.readString();
        this.userGroup = source.readParcelable(UserGroup.class.getClassLoader());
        this.logging = source.readByte() != 0;
    }

    protected User(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.password = in.readString();
        this.userGroup = in.readParcelable(UserGroup.class.getClassLoader());
        this.logging = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
