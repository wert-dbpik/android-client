package ru.wert.bazapik_mobile.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.models.User;

import static ru.wert.bazapik_mobile.ThisApplication.LIST_OF_ALL_USERS;


public class ChatPeopleFragment extends Fragment implements PeopleRecViewAdapter.PeopleClickListener{

    private FragmentManager fm;
    private RecyclerView rv;
    private PeopleRecViewAdapter adapter;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_people, container, false);
        context = getContext();
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
        ((Activity)context).runOnUiThread(()->{
            adapter = new PeopleRecViewAdapter(this, context, items);
            adapter.setClickListener(ChatPeopleFragment.this);
            rv.setAdapter(adapter);
        });
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}