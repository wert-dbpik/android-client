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
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class User extends _BaseEntity implements Item {

    private String name;
    private String password;
    private UserGroup userGroup;
    private boolean logging; //следует ли пользователя логировать

    @Override
    public String toUsefulString() {
        return name;
    }


}
