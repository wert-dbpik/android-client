package ru.wert.bazapik_mobile.data.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Класс позволяет создавать конструктор без поля Long id с помощью аннотации Lombok @AllArgsConstructor.
 * Содержит методы getId() и setId()
 */
@Getter
@Setter
public class _BaseEntity {

    protected Long id;
}
