package ru.wert.bazapik_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.models.User;
import ru.wert.bazapik_mobile.data.servicesREST.UserService;

public class Login extends BaseActivity {
    AutoCompleteTextView tvUserName;
    EditText edtPass;
    Button btnEnter;
    List<User> userList;
    User user;
    ImageView logo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvUserName = findViewById(R.id.userName);
        edtPass = findViewById(R.id.password);
        btnEnter = findViewById(R.id.btnEnter);
        logo = findViewById(R.id.imageViewLogo);

        tvUserName.setText(getProp("LAST_USER_NAME"));

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterOffice();
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterOffice();
            }
        });

        //В поле имен вставляем подсказку - список всех юзеров в базе
        new Thread(() -> {
            userList = UserService.getInstance().findAll();
            List<String> userNames = new ArrayList<>();
            for (User user : userList)
                userNames.add(user.getName());
            String[] names = new String[userNames.size()];
            for (int i = 0; i < userNames.size(); i++)
                names[i] = userNames.get(i);

            runOnUiThread(() ->
                    tvUserName.setAdapter(new ArrayAdapter<>(getAppContext(),
                    android.R.layout.simple_dropdown_item_1line, names)));
        }).start();



        edtPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((actionId == EditorInfo.IME_ACTION_DONE) &&
                        (findUserInListByName() != null)){
                    enterOffice();
                    return true;
                }
                return false;
            }
        });

    }

    private boolean checkPassword(){
        User tempUser = findUserInListByName();
        String tempPass = edtPass.getText().toString();
        if(tempUser != null && tempUser.getPassword().equals(tempPass)){
                this.user = tempUser;
                return true;
            }
        return false;
    }

    public void enterOffice(){
        if(checkPassword()) {
            setProp("LAST_USER_NAME", user.getName());
            CurrentUser.getInstance().setUser(user);
            CurrentUser.getInstance().setUserGroup(user.getUserGroup());

            Intent intent = new Intent(getAppContext(), MainActivity.class);
            startActivity(intent);
        }
        else
            showToast("ДОСТУПА НЕТ!");

    }

    private User findUserInListByName(){
        String tempUserName = tvUserName.getText().toString();
        for(User u : userList) {
            if(u.getName().equals(tempUserName))
                return u;
        }
        return null;
    }

}
