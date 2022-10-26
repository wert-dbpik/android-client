package ru.wert.bazapik_mobile.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.UserApiInterface;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.organizer.folders.FoldersFragment;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;

import android.os.Bundle;

import java.util.List;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_USERS;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChatActivity extends AppCompatActivity {

    private FragmentManager fm;
    private ChatRoomsFragment chatRoomsFragment;
    private ChatPeopleFragment chatPeopleFragment;
    private ChatTabPaneFragment chatTabPaneFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fm = getSupportFragmentManager();

        chatPeopleFragment = new ChatPeopleFragment();
        chatRoomsFragment = new ChatRoomsFragment();
        chatTabPaneFragment = new ChatTabPaneFragment();

    }

    public String getRoomName(String roomNameDB) {
        String finalName = "";
        if (roomNameDB.startsWith("one-to-one:")) {
            roomNameDB = roomNameDB.replace("one-to-one:#", "");
            String[] usersId = roomNameDB.split("#", -1);
            for (String id : usersId) {
                User u = findUserById(Long.parseLong(id));
                if (!u.getId().equals(CURRENT_USER.getId())) {
                    finalName = u.getName();
                    break;
                }

            }
        } else
            finalName = roomNameDB;

        return finalName;

    }

    private User findUserById(Long id){
        for(User u : LIST_OF_ALL_USERS){
            if(u.getId().equals(id))
                return u;
        }
        return null;
    }
}