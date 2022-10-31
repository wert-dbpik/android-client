package ru.wert.bazapik_mobile.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.chat.fragments.ChatFragment;
import ru.wert.bazapik_mobile.chat.fragments.DialogFragment;
import ru.wert.bazapik_mobile.chat.fragments.PeopleFragment;
import ru.wert.bazapik_mobile.chat.fragments.RoomsFragment;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_ROOMS;
import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_USERS;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChatActivity extends AppCompatActivity implements
        ChatActivityInteraction {

    private FragmentContainerView keyboardContainer;
    private FragmentContainerView chatContainer;
    private FragmentManager fm;
    private RoomsFragment roomsFragment;
    private PeopleFragment peopleFragment;

    public static final String $ROOM = "room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chat_fragment_container);
//        keyboardContainer = findViewById(R.id.keyboard_container);

        fm = getSupportFragmentManager();

        peopleFragment = new PeopleFragment();
        roomsFragment = new RoomsFragment();

    }

    private User findUserById(Long id){
        for(User u : LIST_OF_ALL_USERS){
            if(u.getId().equals(id))
                return u;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        ChatFragment fr = (ChatFragment) fm.findFragmentById(R.id.chat_fragment_container);
        //Если открыт фрагмент с КОМНАТАМИ возвращаемся в БАЗУ
        if(fr instanceof RoomsFragment) {
            Intent settingsIntent = new Intent(ChatActivity.this, OrganizerActivity.class);
            startActivity(settingsIntent);
        } else if(fr instanceof PeopleFragment){
            openRoomsFragment();
        }else if(fr instanceof DialogFragment){
            openRoomsFragment();
        }
    }

    @Override //ChatActivityInteraction
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
        } else if(roomNameDB.startsWith("group:")){
            roomNameDB = roomNameDB.replace("group:", "");
            finalName = roomNameDB;
        } else
            finalName = roomNameDB;

        return finalName;

    }

    @Override //ChatActivityInteraction
    public void openRoomsFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.chat_fragment_container, roomsFragment);
        ft.commit();
    }
    @Override //ChatActivityInteraction
    public void openPeopleFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        ft.replace(R.id.chat_fragment_container, peopleFragment);
        ft.commit();
    }

    @Override //ChatActivityInteraction
    public Room findRoomByName(String name){
        for(Room r : LIST_OF_ALL_ROOMS){
            if(r.getName().equals(name))
                return r;
        }
        return null;
    }

    @Override //ChatActivityInteraction
    public void openRoom(Room room){
        Fragment dialog = fm.findFragmentByTag(room.getName());
        //Если диалога нет, создаем новый фрагмент
        if(dialog == null){
            Bundle bundle = new Bundle();
            bundle.putParcelable($ROOM, room);
            dialog = new DialogFragment();
            dialog.setArguments(bundle);
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        ft.replace(R.id.chat_fragment_container, dialog);
        ft.commit();

    }

    @Override //ChatActivityInteraction
    public Context getChatContext(){
        return ChatActivity.this;
    }

}