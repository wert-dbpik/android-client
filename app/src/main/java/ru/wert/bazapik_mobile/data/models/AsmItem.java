package ru.wert.bazapik_mobile.data.models;

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
@EqualsAndHashCode(of = {"assemble", "line", "anyPart"}, callSuper = false)
public class AsmItem extends _BaseEntity implements Item {

    private Assemble assemble; //Сборка в которой находится деталь
    private Integer line;
    private AnyPart anyPart;
    private Integer quantity;

    @Override
    public String getName(){
        return toUsefulString();
    }

    @Override
    public String toUsefulString() {
        return assemble.toUsefulString();
    }



}
