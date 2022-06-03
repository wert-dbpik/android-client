package ru.wert.bazapik_mobile.warnings;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import ru.wert.bazapik_mobile.search.SearchActivity;

public class WarningDialog2 {
    private boolean answer = false;
    private AlertDialog dialog;

    public WarningDialog2(Context context, String title, String problem) {
        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(problem)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (arg0, arg1) -> answer = true).create();
    }

    public boolean show(){
        dialog.show();

        return answer;
    }


}
