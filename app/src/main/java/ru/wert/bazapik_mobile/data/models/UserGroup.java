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
public class UserGroup extends _BaseEntity implements Item, Parcelable {

    private String name;

    private boolean administrate;
    private boolean editUsers;
    //----------------------
    private boolean readDrafts;
    private boolean editDrafts;
    private boolean commentDrafts;
    private boolean deleteDrafts;
    //------------------------
    private boolean readProductStructures;
    private boolean editProductStructures;
    private boolean deleteProductStructures;
    //------------------------
    private boolean readMaterials;
    private boolean editMaterials;
    private boolean deleteMaterials;


    @Override
    public String toUsefulString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    //-------------  Percelable    -------------


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeByte(this.administrate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.editUsers ? (byte) 1 : (byte) 0);
        dest.writeByte(this.readDrafts ? (byte) 1 : (byte) 0);
        dest.writeByte(this.editDrafts ? (byte) 1 : (byte) 0);
        dest.writeByte(this.commentDrafts ? (byte) 1 : (byte) 0);
        dest.writeByte(this.deleteDrafts ? (byte) 1 : (byte) 0);
        dest.writeByte(this.readProductStructures ? (byte) 1 : (byte) 0);
        dest.writeByte(this.editProductStructures ? (byte) 1 : (byte) 0);
        dest.writeByte(this.deleteProductStructures ? (byte) 1 : (byte) 0);
        dest.writeByte(this.readMaterials ? (byte) 1 : (byte) 0);
        dest.writeByte(this.editMaterials ? (byte) 1 : (byte) 0);
        dest.writeByte(this.deleteMaterials ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
        this.administrate = source.readByte() != 0;
        this.editUsers = source.readByte() != 0;
        this.readDrafts = source.readByte() != 0;
        this.editDrafts = source.readByte() != 0;
        this.commentDrafts = source.readByte() != 0;
        this.deleteDrafts = source.readByte() != 0;
        this.readProductStructures = source.readByte() != 0;
        this.editProductStructures = source.readByte() != 0;
        this.deleteProductStructures = source.readByte() != 0;
        this.readMaterials = source.readByte() != 0;
        this.editMaterials = source.readByte() != 0;
        this.deleteMaterials = source.readByte() != 0;
    }

    protected UserGroup(Parcel in) {
        this.name = in.readString();
        this.administrate = in.readByte() != 0;
        this.editUsers = in.readByte() != 0;
        this.readDrafts = in.readByte() != 0;
        this.editDrafts = in.readByte() != 0;
        this.commentDrafts = in.readByte() != 0;
        this.deleteDrafts = in.readByte() != 0;
        this.readProductStructures = in.readByte() != 0;
        this.editProductStructures = in.readByte() != 0;
        this.deleteProductStructures = in.readByte() != 0;
        this.readMaterials = in.readByte() != 0;
        this.editMaterials = in.readByte() != 0;
        this.deleteMaterials = in.readByte() != 0;
    }

    public static final Creator<UserGroup> CREATOR = new Creator<UserGroup>() {
        @Override
        public UserGroup createFromParcel(Parcel source) {
            return new UserGroup(source);
        }

        @Override
        public UserGroup[] newArray(int size) {
            return new UserGroup[size];
        }
    };
}
