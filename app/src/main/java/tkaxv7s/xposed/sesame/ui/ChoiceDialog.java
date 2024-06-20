package tkaxv7s.xposed.sesame.ui;

import android.app.AlertDialog;
import android.content.Context;

import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;

public class ChoiceDialog {

    public static void show(Context c, CharSequence title, ChoiceModelField choiceModelField) {
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setSingleChoiceItems(choiceModelField.getChoiceArray(), choiceModelField.getValue(),
                        (p1, p2) -> choiceModelField.setValue(p2))
                .setPositiveButton(c.getString(R.string.ok), null)
                .create().show();
    }

}
