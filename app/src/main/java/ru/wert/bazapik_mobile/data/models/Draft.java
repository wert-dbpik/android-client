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
@EqualsAndHashCode(of = {"passport"}, callSuper = false)
public class Draft extends _BaseEntity implements Item, Parcelable, Comparable<Draft> {

    private Passport passport;
    private Product initialProduct;
    private Folder folder;
    private String initialDraftName;
    private String extension;
    private Integer draftType;
    private Integer pageNumber;
    private Integer status;
    private String creationTime; //LocalDateTime
    private User creationUser;
    private String withdrawalTime; //LocalDateTime
    private User withdrawalUser;
    private String note;


    @Override
    public String getName() {
        return passport.getName();
    }

    @Override
    public String toUsefulString() {
        return passport.toUsefulString();
    }

    @Override
    public int compareTo(Draft o) {
        return toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }

    public String getFileName(){
        return
                getId() + "." + getExtension();
    }

    // Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeParcelable(this.passport, flags);
        dest.writeParcelable(this.initialProduct, flags);
        dest.writeParcelable(this.folder, flags);
        dest.writeString(this.initialDraftName);
        dest.writeString(this.extension);
        dest.writeValue(this.draftType);
        dest.writeValue(this.pageNumber);
        dest.writeValue(this.status);
        dest.writeString(this.creationTime);
        dest.writeParcelable(this.creationUser, flags);
        dest.writeString(this.withdrawalTime);
        dest.writeParcelable(this.withdrawalUser, flags);
        dest.writeString(this.note);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.passport = source.readParcelable(Passport.class.getClassLoader());
        this.initialProduct = source.readParcelable(Product.class.getClassLoader());
        this.folder = source.readParcelable(Folder.class.getClassLoader());
        this.initialDraftName = source.readString();
        this.extension = source.readString();
        this.draftType = (Integer) source.readValue(Integer.class.getClassLoader());
        this.pageNumber = (Integer) source.readValue(Integer.class.getClassLoader());
        this.status = (Integer) source.readValue(Integer.class.getClassLoader());
        this.creationTime = source.readString();
        this.creationUser = source.readParcelable(User.class.getClassLoader());
        this.withdrawalTime = source.readString();
        this.withdrawalUser = source.readParcelable(User.class.getClassLoader());
        this.note = source.readString();
    }

    protected Draft(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.passport = in.readParcelable(Passport.class.getClassLoader());
        this.initialProduct = in.readParcelable(Product.class.getClassLoader());
        this.folder = in.readParcelable(Folder.class.getClassLoader());
        this.initialDraftName = in.readString();
        this.extension = in.readString();
        this.draftType = (Integer) in.readValue(Integer.class.getClassLoader());
        this.pageNumber = (Integer) in.readValue(Integer.class.getClassLoader());
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.creationTime = in.readString();
        this.creationUser = in.readParcelable(User.class.getClassLoader());
        this.withdrawalTime = in.readString();
        this.withdrawalUser = in.readParcelable(User.class.getClassLoader());
        this.note = in.readString();
    }

    public static final Creator<Draft> CREATOR = new Creator<Draft>() {
        @Override
        public Draft createFromParcel(Parcel source) {
            return new Draft(source);
        }

        @Override
        public Draft[] newArray(int size) {
            return new Draft[size];
        }
    };
}
