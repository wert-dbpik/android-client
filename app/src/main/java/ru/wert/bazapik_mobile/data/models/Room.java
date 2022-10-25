package ru.wert.bazapik_mobile.data.models;

import lombok.*;
import ru.wert.bazapik_mobile.data.interfaces.Item;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class Room extends _BaseEntity implements Item {

    private String name; //заголовок чата, его название
    private User creator; //id пользователя создавшего чат
    private List<User> roommates;


    @Override
    public String toUsefulString() {
        return name;
    }
}
