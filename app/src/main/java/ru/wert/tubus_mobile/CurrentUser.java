package ru.wert.tubus_mobile;

import lombok.Getter;
import lombok.Setter;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.models.UserGroup;

@Getter
@Setter
public class CurrentUser {

    private static CurrentUser instance;
    private User user;
    private UserGroup userGroup;

    private CurrentUser() {
    }

    public static CurrentUser getInstance(){
        if(instance == null){
            instance = new CurrentUser();
        }
        return instance;
    }

}
