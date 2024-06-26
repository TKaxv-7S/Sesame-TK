package tkaxv7s.xposed.sesame.model.task.gameCenter;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;

/**
 * 游戏中心
 *
 * @author xiong
 */
public class GameCenter extends ModelTask {

    private static final String TAG = GameCenter.class.getSimpleName();

    /**
     * 是否启用游戏中心
     */
    private BooleanModelField gameCenter;
    /**
     * 是否启用签到
     */
    private BooleanModelField bmSignIn;
    /**
     * 是否启用全部领取
     */
    private BooleanModelField bmBatchReceive;

    @Override
    public String setName() {
        return "游戏中心";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(gameCenter = new BooleanModelField("gameCenter", "开启游戏中心", false));
        modelFields.addField(bmSignIn = new BooleanModelField("bmSignIn", "开启 | 签到", false));
        modelFields.addField(bmBatchReceive = new BooleanModelField("bmBatchReceive", "开启 | 领取", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return gameCenter.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    /**
     * 执行
     */
    @Override
    public void run() {
        if (bmSignIn.getValue()) {
            signIn();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
        if (bmBatchReceive.getValue()) {
            batchReceive();
        }
    }

    /**
     * 签到
     */
    private void signIn() {
        try {
            String str = GameCenterRpcCall.querySignInBall();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".signIn.querySignInBall", jsonObject.optString("resultDesc"));
                return;
            }
            str = JsonUtil.getValueByPath(jsonObject, "data.signInBallModule.signInStatus");
            if (String.valueOf(true).equals(str)) {
                return;
            }
            str = GameCenterRpcCall.continueSignIn();
            jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".signIn.continueSignIn", jsonObject.optString("resultDesc"));
                return;
            }
            Log.other("游戏中心🎮签到成功");
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /**
     * 全部领取
     */
    private void batchReceive() {
        try {
            String str = GameCenterRpcCall.queryPointBallList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".batchReceive.queryPointBallList", jsonObject.optString("resultDesc"));
                return;
            }
            JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.pointBallList");
            if (jsonArray == null || jsonArray.length() == 0) {
                return;
            }
            str = GameCenterRpcCall.batchReceivePointBall();
            jsonObject = new JSONObject(str);
            if (jsonObject.getBoolean("success")) {
                Log.other("游戏中心🎮全部领取成功[" + JsonUtil.getValueByPath(jsonObject, "data.totalAmount") + "]乐豆");
            } else {
                Log.i(TAG + ".batchReceive.batchReceivePointBall", jsonObject.optString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchReceive err:");
            Log.printStackTrace(TAG, th);
        }
    }

}
