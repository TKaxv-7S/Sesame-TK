package tkaxv7s.xposed.sesame.model.task.welfareCenter;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.TimeUtil;

import java.util.Iterator;


/**
 * 网商银行
 *
 * @author xiong
 */
public class WelfareCenter extends ModelTask {
    private static final String TAG = WelfareCenter.class.getSimpleName();

    /**
     * 执行间隔
     */
    private Integer executeIntervalInt;
    private final BooleanModelField welfareCenter = new BooleanModelField("welfareCenter", "开启网商银行", false);
    private final IntegerModelField executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 2000);
    private final BooleanModelField welfareCenterProfit = new BooleanModelField("welfareCenterProfit", "福利金 | 领奖", false);
    private final BooleanModelField welfareCenterTask = new BooleanModelField("welfareCenterTask", "福利金 | 任务", false);
    private final BooleanModelField welfareCenterWSTask = new BooleanModelField("welfareCenterWSTask", "网商银行 | 任务", false);
    private final BooleanModelField welfareCenterWSLuckDraw = new BooleanModelField("welfareCenterWSLuckDraw", "网商银行 | 抽奖", false);

    @Override
    public String setName() {
        return "网商银行";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(welfareCenter);
        modelFields.addField(executeInterval);
        modelFields.addField(welfareCenterProfit);
        modelFields.addField(welfareCenterTask);
        modelFields.addField(welfareCenterWSTask);
        modelFields.addField(welfareCenterWSLuckDraw);
        return modelFields;
    }

    @Override
    public Boolean check() {
        return welfareCenter.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        executeIntervalInt = Math.max(executeInterval.getValue(), 2000);
        //TODO：待处理，检查未开通的接口
//            String s = WelfareCenterRpcCall.welfareIndex();
//            try {
//                JSONObject jo = new JSONObject(s);
//                if (!jo.getBoolean("success")) {
//                    Log.i(TAG, jo.optString("resultDesc"));
//                    return;
//                }
//                JSONArray result = jo.getJSONArray("sections");
//                if (result.length()==0) {
//                    Log.other("福利金🤑未开通");
//                    return;
//                }
//            } catch (Throwable th) {
//                Log.i(TAG, "index err:");
//                Log.printStackTrace(TAG, th);
//            }
        //1.会报错，queryEnableVirtualProfitV2接口返回success=false
        //2.不会报错，taskDetailList无数据
        if (welfareCenterTask.getValue()) {
            //赚福利金
            WelfareCenterRpcCall.doTask("AP1269301", TAG, "福利金🤑");
        }
        if (welfareCenterWSTask.getValue()) {
            WelfareCenterRpcCall.doTask("AP12202921", TAG, "网商银行🏦");
        }
        if (welfareCenterWSLuckDraw.getValue()) {
            playTrigger();
        }
        if (welfareCenterProfit.getValue()) {
            batchUseVirtualProfit();
        }
    }

    /**
     * 批量领取奖励、签到
     */
    private void batchUseVirtualProfit() {
        try {
            String signInSceneId = "PLAY102815727";
            String str = WelfareCenterRpcCall.queryEnableVirtualProfitV2(signInSceneId);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".batchUseVirtualProfit", jsonObject.optString("resultDesc"));
                return;
            }
            JSONObject result = jsonObject.getJSONObject("result");
            JSONArray virtualProfitList = result.getJSONArray("virtualProfitList");
            for (int i = 0; i < virtualProfitList.length(); i++) {
                JSONObject object = virtualProfitList.getJSONObject(i);
                if ("signin".equals(object.getString("type"))) {
                    signIn(signInSceneId);
                    continue;
                }
                JSONArray virtualProfitIds = object.optJSONArray("virtualProfitIds");
                if (virtualProfitIds == null || virtualProfitIds.length() <= 0) {
                    continue;
                }
                str = WelfareCenterRpcCall.batchUseVirtualProfit(virtualProfitIds);
                result = new JSONObject(str);
                if (!result.getBoolean("success")) {
                    Log.i(TAG + ".batchUseVirtualProfit", result.optString("resultDesc"));
                    continue;
                }
                Log.other("福利金🤑获得[" + object.getString("sceneDesc") + "]" + object.getString("reward") + "×" + virtualProfitIds.length());
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchUseVirtualProfit err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 签到
     *
     * @param sceneId sceneId
     */
    private void signIn(String sceneId) {
        try {
            String str = WelfareCenterRpcCall.signInTrigger(sceneId);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".signIn", jsonObject.optString("resultDesc"));
                return;
            }
            Log.other("福利金🤑签到成功" + JsonUtil.getValueByPath(jsonObject, "result.prizeOrderDTOList.[0].price"));
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private void playTrigger() {
        try {
            String str = WelfareCenterRpcCall.queryCert(new String[]{"CT02048186", "CT32675397"});
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".playTrigger", jsonObject.optString("resultDesc"));
                return;
            }
            jsonObject = (JSONObject) JsonUtil.getValueByPathObject(jsonObject, "result.cert");
            if (jsonObject == null) {
                return;
            }
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int count = jsonObject.getInt(key);
                for (int j = 0; j < count; j++) {
                    str = WelfareCenterRpcCall.playTrigger("PLAY100576638");
                    TimeUtil.sleep(500);
                    jsonObject = new JSONObject(str);
                    if (!jsonObject.getBoolean("success")) {
                        Log.i(TAG + ".playTrigger", jsonObject.optString("resultDesc"));
                        return;
                    }
                    JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "result.extInfo.result.sendResult.prizeSendOrderList");
                    if (jsonArray == null) {
                        continue;
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        Log.other("网商银行🏦获得[" + jsonObject.getString("prizeName") + "]");
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(executeIntervalInt);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }
}