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
@EqualsAndHashCode(of = {"passport"}, callSuper = false)
public class Draft extends _BaseEntity implements Item, Comparable<Draft> {

    private Passport passport;
    private Product initialProduct;
    private Folder folder;
    private String initialDraftName;
    private String extension;
    private Integer draftType;
    private Integer pageNumber;
    private Integer status;
    private String creationTime; //LocalDateTime
    private User creationUser;
    private String withdrawalTime; //LocalDateTime
    private User withdrawalUser;
    private String note;


    @Override
    public String getName() {
        return passport.getName();
    }

    @Override
    public String toUsefulString() {
        return passport.toUsefulString();
    }

    @Override
    public int compareTo(Draft o) {
        return toUsefulString().toLowerCase().compareTo(o.toUsefulString().toLowerCase());
    }

    public String getFileName(){
        return
                getId() + "." + getExtension();
    }


}
