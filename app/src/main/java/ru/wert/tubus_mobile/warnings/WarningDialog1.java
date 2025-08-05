package ru.wert.tubus_mobile.warnings;

import android.app.AlertDialog;
import android.content.Context;

public class WarningDialog1 {

    public void show(Context context, String title, String problem){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(problem);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> dialog.dismiss());
//        alertDialog.setCancelable(false);
//        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
//                (dialogInterface, i) -> {
//                    //Ничего не делать
//                });

        alertDialog.show();
    }


}
