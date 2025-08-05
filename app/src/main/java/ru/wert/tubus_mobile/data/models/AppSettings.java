package ru.wert.tubus_mobile.data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.wert.tubus_mobile.data.interfaces.Item;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class AppSettings extends _BaseEntity implements Item {

    private String name; //Наименование группы настроек
    private User user;   //Пользователь, к которому относятся эти настройки
    private Integer monitor; //монитор

    @Override
    public String toUsefulString() {
        return null; //Пока нет применения
    }


}
