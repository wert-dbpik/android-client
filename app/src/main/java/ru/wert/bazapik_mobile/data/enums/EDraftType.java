package ru.wert.bazapik_mobile.data.enums;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public enum EDraftType {

    DETAIL(0, "Деталь", "ДЕТ"),
    ASSEMBLE(1, "Сборка", "СБ"),
    SPECIFICATION(2, "Спецификация", "СП"),
    PACKAGE(3, "Упаковка", "УП"),
    TOOL(4, "Кондуктор", "КОНД"),
    MARKING(5, "Маркировка", "МРК"),
    IMAGE_3D(6, "3D изображение", "3D"),
    DRAW(7, "CAD документ", "CAD"),
    MOUNT(8, "Монтажная схема", "ИМ"),
    ELECTRIC(9, "Электрическая схема", "ЭЛ");

    private final Integer typeId;
    private final String typeName;
    private final String shortName;

    EDraftType(Integer typeId, String typeName, String shortName) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.shortName = shortName;
    }

    /**
     * Возвращает тип документа в зависимости от его id
     * @param typeId Integer
     * @return DraftType
     */
    public static EDraftType getDraftTypeById(Integer typeId) {

        for(EDraftType type : EDraftType.values()){
            if(type.typeId.equals(typeId))
                return type;
        }
        return null;
    }

    /**
     * Возвращает список типов документов
     * @return ObservableList<String>
     */
    public static List<String> getAllDraftsTypes(){
        List<String> types = new ArrayList<>();
        for(EDraftType type : EDraftType.values()) {
            types.add(type.shortName);
        }
        return types;
    }
}
