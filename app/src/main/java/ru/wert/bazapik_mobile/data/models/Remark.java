package ru.wert.bazapik_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@EqualsAndHashCode(of = {"user", "text"}, callSuper = false)
public class Remark extends _BaseEntity implements Item, Comparable, Parcelable {

    private Passport passport;
    private User user;
    private String text;
    private String creationTime;

    private List<Pic> picsInRemark;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String toUsefulString() {
        return user + ": " + text + "pics: " + picsInRemark.toString();
    }

    @Override
    public int compareTo(Object o) {
        return ((Remark)o).getCreationTime().compareTo(creationTime);
//        return creationTime.compareTo(((Remark)o).getCreationTime());
    }

    //==============   Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeParcelable(this.passport, flags);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.text);
        dest.writeString(this.creationTime);
        dest.writeTypedList(this.picsInRemark);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.passport = source.readParcelable(Passport.class.getClassLoader());
        this.user = source.readParcelable(User.class.getClassLoader());
        this.text = source.readString();
        this.creationTime = source.readString();
        this.picsInRemark = source.createTypedArrayList(Pic.CREATOR);
    }

    protected Remark(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.passport = in.readParcelable(Passport.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.text = in.readString();
        this.creationTime = in.readString();
        this.picsInRemark = in.createTypedArrayList(Pic.CREATOR);
    }

    public static final Creator<Remark> CREATOR = new Creator<Remark>() {
        @Override
        public Remark createFromParcel(Parcel source) {
            return new Remark(source);
        }

        @Override
        public Remark[] newArray(int size) {
            return new Remark[size];
        }
    };
}
