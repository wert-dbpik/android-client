package ru.wert.bazapik_mobile.warnings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

import ru.wert.bazapik_mobile.search.SearchActivity;

public class Warning1 {

    public void show(Context context, String title, String problem){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(problem);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());

        alertDialog.show();
    }


}
