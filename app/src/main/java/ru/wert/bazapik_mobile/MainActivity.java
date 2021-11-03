package ru.wert.bazapik_mobile;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.userList);

        GetAllUsers getAllUsers = new GetAllUsers(getApplication(), listView);
        getAllUsers.start();

    }
}

class GetAllUsers extends Thread{
    private ListView listView;
    private Context context;

    public GetAllUsers(Context context, ListView listView) {
        this.listView = listView;
        this.context = context;
    }

    @Override
    public void run(){

        List<User> usersInDB = UserService.getInstance().findAll();
        List<String> userNames = new ArrayList<>();
        for(User user: usersInDB)
            userNames.add(user.getName());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context ,android.R.layout.simple_list_item_1, userNames);

        listView.post(() -> listView.setAdapter(adapter));
    }

}

