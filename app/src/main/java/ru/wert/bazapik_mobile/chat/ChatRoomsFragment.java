package ru.wert.bazapik_mobile.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
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
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.AppWarnings;

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChatRoomsFragment extends Fragment implements ChatFragment, RoomsRecViewAdapter.RoomsClickListener{

    private ChatActivityInteraction chatActivity;
    private FragmentManager fm;
    private RecyclerView rv;
    private RoomsRecViewAdapter adapter;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String SAVED_STATE_BUNDLE = "saved_state_bundle";

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        chatActivity = (ChatActivityInteraction) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_rooms, container, false);
        rv = view.findViewById(R.id.recycle_view_rooms);

        createRecViewOfFoundRooms();

        return view;
    }

    private void createRecViewOfFoundRooms(){
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        RoomApiInterface roomApiInterface = RetrofitClient.getInstance().getRetrofit().create(RoomApiInterface.class);
        Call<List<Room>> call = roomApiInterface.getAll();
        call.enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if(response.isSuccessful()){
                    List<Room> foundRooms = new ArrayList<>();
                    List<Room> rooms = response.body();
                    for(Room room : rooms){
                        if(room.getRoommates().contains(CURRENT_USER))
                            foundRooms.add(room);
                    }
                    fillRecViewWithItems(foundRooms);
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(chatActivity.getChatContext());
            }
        });

        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void fillRecViewWithItems(List<Room> items){
        ((Activity)chatActivity.getChatContext()).runOnUiThread(()->{
            adapter = new RoomsRecViewAdapter(this, chatActivity.getChatContext(), items);
            adapter.setClickListener(ChatRoomsFragment.this);
            rv.setAdapter(adapter);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Room room = adapter.getItem(position);
        chatActivity.openRoom(room);
    }
}