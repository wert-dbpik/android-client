package ru.wert.bazapik_mobile.chat;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatCards {

    public void create(Context context, LinearLayout llMessage, String text){
        TextView textView = new TextView(context);
        textView.setText(text);
        llMessage.addView(textView);
    }
}
