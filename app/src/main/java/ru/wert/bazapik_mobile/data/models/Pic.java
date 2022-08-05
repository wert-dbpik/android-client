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
@EqualsAndHashCode(of = {"user", "time"}, callSuper = false)
public class Pic extends _BaseEntity implements Item, Parcelable {
    private String extension;
    private Integer width;
    private Integer height;
    private User user;
    private String time;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String toUsefulString() {
        return user.getName() + ": " + time;
    }

    //-----------  Parcelable  -----------


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.extension);
        dest.writeValue(this.width);
        dest.writeValue(this.height);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.time);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.extension = source.readString();
        this.width = (Integer) source.readValue(Integer.class.getClassLoader());
        this.height = (Integer) source.readValue(Integer.class.getClassLoader());
        this.user = source.readParcelable(User.class.getClassLoader());
        this.time = source.readString();
    }

    protected Pic(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.extension = in.readString();
        this.width = (Integer) in.readValue(Integer.class.getClassLoader());
        this.height = (Integer) in.readValue(Integer.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.time = in.readString();
    }

    public static final Creator<Pic> CREATOR = new Creator<Pic>() {
        @Override
        public Pic createFromParcel(Parcel source) {
            return new Pic(source);
        }

        @Override
        public Pic[] newArray(int size) {
            return new Pic[size];
        }
    };
}
