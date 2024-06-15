package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.modelFieldExt.ChoiceModelField;

public class ChoiceDialog {

    public static void show(Context c, CharSequence title, ChoiceModelField choiceModelField) {
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setSingleChoiceItems(choiceModelField.getList(), choiceModelField.getValue(),
                        (p1, p2) -> choiceModelField.setValue(p2))
                .setPositiveButton(c.getString(R.string.ok), null)
                .create().show();
    }

}
