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

    }

//    public static Context getAppContext(){
//        return getAppContext();
//    }
}


