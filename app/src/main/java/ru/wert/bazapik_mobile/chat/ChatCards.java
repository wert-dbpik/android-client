package ru.wert.bazapik_mobile.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.wert.bazapik_mobile.R;

public class ChatCards {

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

    /**
     * Стиль ИСХОДЯЩИХ сообщений
     * @param context Context
     * @param llMainContainer LinearLayout
     * @param llSelectedContainer LinearLayout
     * @param llFitContainer LinearLayout
     * @param sender TextView
     * @param date TextView
     * @param llMessageContainer LinearLayout
     * @param time TextView
     */
    public static void useMessageOUT_Style(Context context,
                                           LinearLayout llMainContainer, LinearLayout llSelectedContainer, LinearLayout llFitContainer,
                                           TextView sender, TextView date, LinearLayout llMessageContainer, TextView time){

        useCommonStyle(llMainContainer, llSelectedContainer, time);
        llFitContainer.removeView(sender);
        llFitContainer.removeView(date);
        //Смещаем вправо
        llMainContainer.setGravity(Gravity.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        llFitContainer.setLayoutParams(params);


        llFitContainer.setBackground(context.getDrawable(R.drawable.borders_message_out));
        llMessageContainer.setBackgroundColor(context.getColor(R.color.out_message_background));

    }

    public static void useMessageIN_Style(Context context,
                                          LinearLayout llMainContainer, LinearLayout llSelectedContainer, LinearLayout llFitContainer,
                                          TextView sender, TextView date, LinearLayout llMessageContainer, TextView time){

        useCommonStyle(llMainContainer, llSelectedContainer, time);

//        llFitContainer.addView(sender, 1);
//        llFitContainer.addView(date, 1);

        sender.setTextColor(Color.BLUE);
        date.setTextColor(Color.BLUE);
        //Смещаем влево
        sender.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        llMainContainer.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;
        llFitContainer.setLayoutParams(params);

//        llFitContainer.setBackgroundColor(context.getResources().getColor(R.color.in_message_background, null));
        llFitContainer.setBackground(context.getDrawable(R.drawable.borders_message_in));
        llMessageContainer.setBackgroundColor(context.getColor(R.color.in_message_background));
    }

    private static void useCommonStyle(LinearLayout llMainContainer, LinearLayout llSelectedContainer, TextView time){
        llSelectedContainer.setBackgroundColor(Color.WHITE);
        llMainContainer.setBackgroundColor(Color.WHITE);
        time.setTextColor(Color.GRAY);

    }
}
