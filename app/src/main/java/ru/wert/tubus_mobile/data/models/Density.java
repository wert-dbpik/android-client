package ru.wert.tubus_mobile.data.models;

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
public class Density extends _BaseEntity implements Item {

    private String name;
    private double amount;
    private String note;

    @Override
    public String toUsefulString() {
        return name;
    }


}
