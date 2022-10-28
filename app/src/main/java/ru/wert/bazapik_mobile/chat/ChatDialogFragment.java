package ru.wert.bazapik_mobile.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.MessageApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.RoomApiInterface;
import ru.wert.bazapik_mobile.data.models.Message;
import ru.wert.bazapik_mobile.data.models.Room;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.warnings.AppWarnings;

import static ru.wert.bazapik_mobile.chat.ChatActivity.$ROOM;
import static ru.wert.bazapik_mobile.constants.Consts.CURRENT_USER;


public class ChatDialogFragment extends Fragment implements ChatFragment {

    private TextView tvChatName;
    private RecyclerView rv;
    private TextView etMessage;
    private ImageButton ibtnSendPhoto;
    private ImageButton ibtnSendImage;
    private ImageButton ibtnSendLink;
    private ImageButton ibtnSend;

    private DialogRecViewAdapter adapter;

    private ChatActivityInteraction chatActivity;

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
        View view = inflater.inflate(R.layout.fragment_chat_dialog, container, false);

        Room room = getArguments().getParcelable($ROOM);

        tvChatName = view.findViewById(R.id.tvChatName);
        rv = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        ibtnSendPhoto = view.findViewById(R.id.ibtnSendPhoto);
        ibtnSendImage = view.findViewById(R.id.ibtnSendImage);
        ibtnSendLink = view.findViewById(R.id.ibtnSendLink);
        ibtnSend = view.findViewById(R.id.ibtnSend);

        tvChatName.setText(chatActivity.getRoomName(room.getName()));
        if(room.getName().startsWith("one-to-one")) tvChatName.setTextColor(Color.YELLOW);
        else if (room.getName().startsWith("group")) tvChatName.setTextColor(Color.CYAN);

        createRecViewOfFoundMessages();

        return view;
    }


    private void createRecViewOfFoundMessages(){
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        MessageApiInterface roomApiInterface = RetrofitClient.getInstance().getRetrofit().create(MessageApiInterface.class);
        Call<List<Message>> call = roomApiInterface.getAll();
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if(response.isSuccessful()){
                    fillRecViewWithItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(chatActivity.getChatContext());
            }
        });

        //Для красоты используем разделитель между элементами списка
        rv.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    public void fillRecViewWithItems(List<Message> items){
        ((Activity)chatActivity.getChatContext()).runOnUiThread(()->{
            adapter = new DialogRecViewAdapter(this, chatActivity.getChatContext(), items);
            rv.setAdapter(adapter);
        });
    }
}