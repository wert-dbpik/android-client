package ru.wert.tubus_mobile.data.models;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.wert.tubus_mobile.data.interfaces.Item;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class MatType extends _BaseEntity implements Item, Serializable {

    private String name;
    private String note;

    @Override
    public String toUsefulString() {
        return name;
    }


}
