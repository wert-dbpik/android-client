package ru.wert.tubus_mobile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

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
@EqualsAndHashCode(of = {"name", "secondName"}, callSuper = false)
public class AnyPart extends _BaseEntity implements Item , Parcelable {

    private AnyPartType anyPartType;
    private String name;
    private String secondName;

    @Override
    public String toUsefulString() {
        if(secondName == null)
            return name;
        return name + BLConst.SEPARATOR + secondName;
    }


    //Parcelable


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeSerializable(this.anyPartType);
        dest.writeString(this.name);
        dest.writeString(this.secondName);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.anyPartType = (AnyPartType) source.readSerializable();
        this.name = source.readString();
        this.secondName = source.readString();
    }

    protected AnyPart(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.anyPartType = (AnyPartType) in.readSerializable();
        this.name = in.readString();
        this.secondName = in.readString();
    }

    public static final Creator<AnyPart> CREATOR = new Creator<AnyPart>() {
        @Override
        public AnyPart createFromParcel(Parcel source) {
            return new AnyPart(source);
        }

        @Override
        public AnyPart[] newArray(int size) {
            return new AnyPart[size];
        }
    };
}
