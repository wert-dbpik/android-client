package ru.wert.bazapik_mobile.data.models;

import java.io.Serializable;
import java.util.Set;

//import javax.xml.soap.Detail;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.util.BLConst;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"decNumber"}, callSuper = false)
public class Folder extends _BaseEntity implements Item, Comparable<Folder>, Serializable {

    private String decNumber;
    private String name;
    private String note;
    private Set<Product> productsInFolder;
    private Set<Detail> detailsInFolder;

    @Override
    public int compareTo(Folder o) {
        if(o.getName().equals(BLConst.RAZLOZHENO))
            return 0;
        return
                toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }


    @Override
    public String toUsefulString() {
        return name + " : " + decNumber;
    }


}
