package ru.wert.bazapik_mobile.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

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

import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;

public class ChatRoomsFragment extends Fragment implements RoomsRecViewAdapter.RoomsClickListener {

    private FragmentManager fm;
    private RecyclerView rv;
    private RoomsRecViewAdapter adapter;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_rooms, container, false);
        context = getContext();
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
                Log.e("ЖОПА", "onFailure: ",t.fillInStackTrace());
            }
        });

        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void fillRecViewWithItems(List<Room> items){
        ((Activity)context).runOnUiThread(()->{
            adapter = new RoomsRecViewAdapter(this, context, items);
            adapter.setClickListener(ChatRoomsFragment.this);
            rv.setAdapter(adapter);
        });
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}