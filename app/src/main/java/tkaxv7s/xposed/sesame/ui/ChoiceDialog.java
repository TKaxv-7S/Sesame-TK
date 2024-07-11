package tkaxv7s.xposed.sesame.ui;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;

public class ChoiceDialog {

    public static void show(Context c, CharSequence title, ChoiceModelField choiceModelField) {
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setSingleChoiceItems(choiceModelField.getExpandKey(), choiceModelField.getValue(),
                        (p1, p2) -> choiceModelField.setObjectValue(p2))
                .setPositiveButton(c.getString(R.string.ok), null)
                .create().show();
    }

}
