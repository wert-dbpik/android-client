package ru.wert.bazapik_mobile.data.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Set;

//import javax.xml.soap.Detail;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.util.BLConst;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"productGroup", "name"}, callSuper = false)
public class Folder extends _BaseEntity implements Item, Comparable<Folder>, Serializable {

    private ProductGroup productGroup;
    private String name;
    private String note;

    @Override
    public int compareTo(@NotNull Folder o) {
        if(o.getName().equals(BLConst.RAZLOZHENO))
            return 0;
        return
                toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }


    @Override
    public String toUsefulString() {
        return name;
    }
}
