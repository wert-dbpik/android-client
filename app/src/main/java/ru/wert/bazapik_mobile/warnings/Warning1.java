package ru.wert.bazapik_mobile.warnings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Warning1 {

    public static void show(Context context, String title, String problem){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(problem);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}
