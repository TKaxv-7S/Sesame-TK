package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.model.AntFarm.SendType;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Config.RecallAnimalType;

public class ChoiceDialog {

    public static void showSendType(Context c, CharSequence title) {
        new AlertDialog.Builder(c)
            .setTitle(title)
            .setSingleChoiceItems(SendType.nickNames, Config.INSTANCE.getSendType(),
                    (p1, p2) -> Config.INSTANCE.setSendType(p2))
            .setPositiveButton(c.getString(R.string.ok), null)
            .create().show();
    }

    public static void showRecallAnimalType(Context c, CharSequence title) {
        new AlertDialog.Builder(c)
            .setTitle(title)
            .setSingleChoiceItems(RecallAnimalType.nickNames, Config.INSTANCE.getRecallAnimalType(),
                    (p1, p2) -> Config.INSTANCE.setRecallAnimalType(p2))
            .setPositiveButton(c.getString(R.string.ok), null)
            .create().show();
    }

}
