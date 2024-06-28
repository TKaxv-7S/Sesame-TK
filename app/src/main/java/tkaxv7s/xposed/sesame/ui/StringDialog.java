package tkaxv7s.xposed.sesame.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.util.Log;

public class StringDialog {

    private static ModelField modelField;

    public static void showEditDialog(Context c, CharSequence title, ModelField modelField) {
        showEditDialog(c, title, modelField, null);
    }

    public static void showEditDialog(Context c, CharSequence title, ModelField modelField, String msg) {
        StringDialog.modelField = modelField;
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

    public static void showReadDialog(Context c, CharSequence title, ModelField modelField) {
        showReadDialog(c, title, modelField, null);
    }

    public static void showReadDialog(Context c, CharSequence title, ModelField modelField, String msg) {
        StringDialog.modelField = modelField;
        AlertDialog readDialog = getReadDialog(c);
        if (msg != null) {
            readDialog.setTitle(title);
            readDialog.setMessage(msg);
        } else {
            readDialog.setTitle(title);
        }
        readDialog.show();
    }

    private static AlertDialog getReadDialog(Context c) {
        EditText edt = new EditText(c);
        edt.setInputType(InputType.TYPE_NULL);
        edt.setTextColor(Color.GRAY);
        AlertDialog readDialog = new AlertDialog.Builder(c)
                .setTitle("title")
                .setView(edt)
                .create();
        edt.setText(String.valueOf(modelField.getConfigValue()));
        return readDialog;
    }

}
