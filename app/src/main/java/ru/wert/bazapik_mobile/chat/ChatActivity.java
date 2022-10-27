package ru.wert.bazapik_mobile.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ru.wert.bazapik_mobile.ChangePassActivity;
import ru.wert.bazapik_mobile.LoginActivity;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.dataPreloading.DataLoadingActivity;
import ru.wert.bazapik_mobile.organizer.FilterDialog;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerFragment;
import ru.wert.bazapik_mobile.organizer.folders.FoldersFragment;
import ru.wert.bazapik_mobile.organizer.folders.FoldersRecViewAdapter;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;
import ru.wert.bazapik_mobile.settings.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_ROOMS;
import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_USERS;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChatActivity extends AppCompatActivity implements
        ChatActivityInteraction {

    private FragmentContainerView keyboardContainer;
    private FragmentContainerView chatContainer;
    private FragmentManager fm;
    private ChatRoomsFragment chatRoomsFragment;
    private ChatPeopleFragment chatPeopleFragment;

    public static final String $ROOM = "room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chat_fragment_container);
//        keyboardContainer = findViewById(R.id.keyboard_container);

        fm = getSupportFragmentManager();

        chatPeopleFragment = new ChatPeopleFragment();
        chatRoomsFragment = new ChatRoomsFragment();

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
        if(fr instanceof ChatRoomsFragment) {
            Intent settingsIntent = new Intent(ChatActivity.this, OrganizerActivity.class);
            startActivity(settingsIntent);
        } else if(fr instanceof ChatPeopleFragment){
            openRoomsFragment();
        }else if(fr instanceof ChatDialogFragment){
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
        } else
            finalName = roomNameDB;

        return finalName;

    }

    @Override //ChatActivityInteraction
    public void openRoomsFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_right_in, R.animator.to_right_out);
        ft.replace(R.id.chat_fragment_container, chatRoomsFragment);
        ft.commit();
    }
    @Override //ChatActivityInteraction
    public void openPeopleFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.to_left_in, R.animator.to_left_out);
        ft.replace(R.id.chat_fragment_container, chatPeopleFragment);
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
            dialog = new ChatDialogFragment();
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

    //=======================  M E N U  ================================

    /**
     * Создаем меню для окна с поиском
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    /**
     * Обработка выбора меню
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {

            case R.id.action_organiser:
                Intent settingsIntent = new Intent(ChatActivity.this, OrganizerActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_people:
                openPeopleFragment();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}