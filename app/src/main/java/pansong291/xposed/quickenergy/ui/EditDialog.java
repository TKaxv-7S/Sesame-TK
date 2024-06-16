package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.widget.EditText;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.util.Log;

public class EditDialog {

    private static ModelField modelField;

    public static void showEditDialog(Context c, CharSequence title, ModelField modelField) {
        showEditDialog(c, title, modelField, null);
    }

    public static void showEditDialog(Context c, CharSequence title, ModelField modelField, String msg) {
        EditDialog.modelField = modelField;
        AlertDialog editDialog = getEditDialog(c);
        if (msg != null) {
            editDialog.setTitle(title);
            editDialog.setMessage(msg);
        } else {
            editDialog.setTitle(title);
        }
        editDialog.show();
    }

    private static AlertDialog getEditDialog(Context c) {
        EditText edt = new EditText(c);
        AlertDialog editDialog = new AlertDialog.Builder(c)
                .setTitle("title")
                .setView(edt)
                .setPositiveButton(
                        c.getString(R.string.ok),
                        new OnClickListener() {
                            Context context;

                            public OnClickListener setData(Context c) {
                                context = c;
                                return this;
                            }

                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                try {
                                    Editable text = edt.getText();
                                    if (text == null) {
                                        modelField.setConfigValue(null);
                                        return;
                                    }
                                    String textString = text.toString();
                                    if (textString.isEmpty()) {
                                        modelField.setConfigValue(null);
                                        return;
                                    }
                                    modelField.setConfigValue(textString);
                                } catch (Throwable e) {
                                    Log.printStackTrace(e);
                                }
                            }
                        }.setData(c))
                .create();
        edt.setText(String.valueOf(modelField.getConfigValue()));
        return editDialog;
    }

}
