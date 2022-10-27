package ru.wert.bazapik_mobile.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.RoomApiInterface;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.info.InfoActivity;
import ru.wert.bazapik_mobile.warnings.AppWarnings;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_USERS;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;


public class ChatPeopleFragment extends Fragment implements ChatFragment, PeopleRecViewAdapter.PeopleClickListener{
    private ChatActivityInteraction chatActivity;
    private FragmentManager fm;
    private RecyclerView rv;
    private PeopleRecViewAdapter adapter;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SAVED_STATE_BUNDLE = "saved_state_bundle";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        chatActivity = (ChatActivityInteraction) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_BUNDLE, createSaveStateBundle());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null){
            Bundle b = savedInstanceState.getBundle(SAVED_STATE_BUNDLE);

            Parcelable savedRecyclerLayoutState = b.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(rv.getLayoutManager()).onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private Bundle createSaveStateBundle(){
        Bundle bundle = new Bundle();

        Parcelable listState = Objects.requireNonNull(rv.getLayoutManager()).onSaveInstanceState();
        bundle.putParcelable(KEY_RECYCLER_STATE, listState);

        return bundle;
    }

    @Override
    public void onStop() {
        super.onStop();
        onSaveInstanceState(createSaveStateBundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_people, container, false);
        rv = view.findViewById(R.id.recycle_view_people);

        createRecViewOfPeople();

        return view;
    }

    private void createRecViewOfPeople(){
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        fillRecViewWithItems(LIST_OF_ALL_USERS);


        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void fillRecViewWithItems(List<User> items){
        ((Activity)chatActivity.getChatContext()).runOnUiThread(()->{
            adapter = new PeopleRecViewAdapter(this, chatActivity.getChatContext(), items);
            adapter.setClickListener(ChatPeopleFragment.this);
            rv.setAdapter(adapter);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Room room = null;
        //Создаем новую ROOM если ее еще не создано
        Long user1 = CURRENT_USER.getId();
        Long user2 = adapter.getItem(position).getId();
        String roomName = "one-to-one:#" + Math.min(user1, user2) + "#" + Math.max(user1, user2);
        room = chatActivity.findRoomByName(roomName);
        if (room == null) {
            Room newRoom = new Room();
            newRoom.setName(roomName);
            newRoom.setCreator(CURRENT_USER);
            newRoom.setRoommates(Collections.singletonList(CURRENT_USER));

            RoomApiInterface api = RetrofitClient.getInstance().getRetrofit().create(RoomApiInterface.class);
            Call<Room> call = api.create(newRoom);
            call.enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    if(response.isSuccessful()){
                        chatActivity.openRoom(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    AppWarnings.showAlert_NoConnection(chatActivity.getChatContext());
                }
            });
        } else
            chatActivity.openRoom(room);
    }






}