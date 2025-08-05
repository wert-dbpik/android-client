package ru.wert.tubus_mobile.data.models;

import java.io.Serializable;
import java.util.List;

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
public class Assemble extends _BaseEntity implements Item, ItemWithDraft, Comparable<Assemble>, Serializable {

    private AnyPart anyPart;
    private Passport passport;
    private String variant;
    private Coat coat;
    private TechProcess techProcess;
    private Folder folder; //Реальная папка в архиве
    private Draft draft;
    private String note;
    private List<AsmItem> asmItemsInAssemble;

    @Override
    public int compareTo(Assemble o) {
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
