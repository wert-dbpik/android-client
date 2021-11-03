package ru.wert.bazapik_mobile.data.models;

import java.io.Serializable;
import java.util.List;

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
public class AnyPartType extends _BaseEntity implements Item, Serializable {

    private Long parentId;
    private String name;
    private List<AnyPart> partsInGroup;


    @Override
    public String toUsefulString() {
        return name;
    }
}
