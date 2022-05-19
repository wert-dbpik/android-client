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
public class UserGroup extends _BaseEntity implements Item {

    private String name;

    private boolean administrate;
    private boolean editUsers;
    //----------------------
    private boolean readDrafts;
    private boolean editDrafts;
    private boolean commentDrafts;
    private boolean deleteDrafts;
    //------------------------
    private boolean readProductStructures;
    private boolean editProductStructures;
    private boolean deleteProductStructures;
    //------------------------
    private boolean readMaterials;
    private boolean editMaterials;
    private boolean deleteMaterials;


    @Override
    public String toUsefulString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


}
