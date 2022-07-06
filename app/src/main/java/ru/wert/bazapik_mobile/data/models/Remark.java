package ru.wert.bazapik_mobile.data.models;

import java.time.LocalDateTime;

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
@EqualsAndHashCode(of = {"user", "text"}, callSuper = false)
public class Remark extends _BaseEntity implements Item {

    private Passport passport;
    private User user;
    private String text;
    private String creationTime;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String toUsefulString() {
        return user + ": " + text;
    }
}
