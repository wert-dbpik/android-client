package ru.wert.bazapik_mobile.data.models;

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
@EqualsAndHashCode(of = {"prefix", "number", "name"}, callSuper = false)
public class Passport extends _BaseEntity implements Item {

    private Prefix prefix;
    private String number;
    private String name;
    private List<Long> draftIds;

    @Override
    public String toUsefulString() {
        String body = number;
        if(prefix.getName().equals("-"))
            return body;
        else
            return prefix.getName() + "." + body;
    }

}
