package tkaxv7s.xposed.sesame.task.model.kbMember;

import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.Statistics;
import tkaxv7s.xposed.sesame.util.LanguageUtil;
import tkaxv7s.xposed.sesame.R;

public class KBMember extends ModelTask {
    private static final String TAG = KBMember.class.getSimpleName();

    @Override
    public String setName() {
        return LanguageUtil.getString(R.string.koubei);
    }

    public BooleanModelField enableKb;
    public BooleanModelField kbSignIn;

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(enableKb = new BooleanModelField("enableKb", LanguageUtil.getString(R.string.enable_koubei), false));
        modelFields.addField(kbSignIn = new BooleanModelField("kbSignIn", LanguageUtil.getString(R.string.koubei_sign_in), false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return enableKb.getValue() && kbSignIn.getValue() && Statistics.canKbSignInToday();
    }

    @Override
    public Runnable init() {
        return () -> {
            try {
                String s = KBMemberRpcCall.rpcCall_signIn();
                JSONObject jo = new JSONObject(s);
                if (jo.getBoolean("success")) {
                    jo = jo.getJSONObject("data");
                    Log.other("口碑签到📅[第" + jo.getString("dayNo") + "天]#获得" + jo.getString("value") + "积分");
                    Statistics.KbSignInToday();
                } else if (s.contains("\"HAS_SIGN_IN\"")) {
                    Statistics.KbSignInToday();
                } else {
                    Log.i(TAG, jo.getString("errorMessage"));
                }
            } catch (Throwable t) {
                Log.i(TAG, "signIn err:");
                Log.printStackTrace(TAG, t);
            }
        };
    }

}
