package ru.wert.tubus_mobile.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Cards {

    public static void createServiceCard(Context context, LinearLayout llMessageContainer, String text){
        //В первую очередь очищаем контейнер, чтобы не дублировать запись
        llMessageContainer.removeAllViews();

        TextView textView = new TextView(context);
        textView.setTextColor(Color.GRAY);
        textView.setTypeface(null, Typeface.ITALIC);
        textView.setText(String.format("---  %s  ---", text));

        llMessageContainer.addView(textView);
    }

    public static void createTextCard(Context context, LinearLayout llMessageContainer, String text){
        llMessageContainer.removeAllViews();
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        llMessageContainer.addView(textView);
    }

    public static void createPicsCard(Context context, LinearLayout llMessageContainer, String text){
        llMessageContainer.removeAllViews();
    }

    public static void createDraftsCard(Context context, LinearLayout llMessageContainer, String text){
        llMessageContainer.removeAllViews();
    }

    public static void createFoldersCard(Context context, LinearLayout llMessageContainer, String text){
        llMessageContainer.removeAllViews();
    }

    public static void createPassportsCard(Context context, LinearLayout llMessageContainer, String text){
        llMessageContainer.removeAllViews();
    }


}
