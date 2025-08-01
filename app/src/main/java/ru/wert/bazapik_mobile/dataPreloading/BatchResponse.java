package ru.wert.bazapik_mobile.dataPreloading;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.models.User;

@Getter
@Setter
public class BatchResponse {

    private List<User> users;
    private List<Room> rooms;
    private List<ProductGroup> productGroups;
    private List<Draft> drafts;
    private List<Folder> folders;
    private List<Passport> passports;
}
