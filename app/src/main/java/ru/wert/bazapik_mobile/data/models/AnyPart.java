package ru.wert.bazapik_mobile.data.models;

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
@EqualsAndHashCode(of = {"name", "secondName"}, callSuper = false)
public class AnyPart extends _BaseEntity implements Item {

    private AnyPartType anyPartType;
    private String name;
    private String secondName;

    @Override
    public String toUsefulString() {
        if(secondName == null)
            return name;
        return name + BLConst.SEPARATOR + secondName;
    }
}
