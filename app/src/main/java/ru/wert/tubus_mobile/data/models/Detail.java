package ru.wert.tubus_mobile.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.tubus_mobile.data.interfaces.ItemWithDraft;
import ru.wert.tubus_mobile.data.interfaces.Item;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"passport"}, callSuper = false)
public class Detail extends _BaseEntity implements Item, ItemWithDraft, Comparable<Detail>, Serializable {

    private AnyPart part;
    private String krp;
    private Passport passport;
    private String variant;
    private Coat coat;
    private Folder folder; //Реальная папка в архиве
    private Material material;
    private Integer paramA;
    private Integer paramB;
    private Draft draft;
    private String note;

    @Override
    public int compareTo(Detail o) {
        return toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }

    @Override
    public String getName() {
        return passport.getName();
    }

    @Override
    public String toUsefulString() {
        return passport.toUsefulString();
    }


}
