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
@EqualsAndHashCode(of = {"user", "time"}, callSuper = false)
public class Pic extends _BaseEntity implements Item {
    private String extension;
    private User user;
    private String time;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String toUsefulString() {
        return user.getName() + ": " + time;
    }
}
