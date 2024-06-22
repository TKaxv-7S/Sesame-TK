package tkaxv7s.xposed.sesame.task.model.greenFinance;

import org.json.JSONArray;
import org.json.JSONObject;

import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.task.common.ModelTask;
import tkaxv7s.xposed.sesame.task.common.TaskCommon;
import tkaxv7s.xposed.sesame.task.model.welfareCenter.WelfareCenterRpcCall;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.*;

/**
 * @author Constanline
 * @since 2023/09/08
 */
public class GreenFinance extends ModelTask {
    private static final String TAG = GreenFinance.class.getSimpleName();

    private Integer executeIntervalInt;
    private BooleanModelField greenFinance;
    private IntegerModelField executeInterval;
    private BooleanModelField greenFinanceLsxd;
    private BooleanModelField greenFinanceLsbg;
    private BooleanModelField greenFinanceLscg;
    private BooleanModelField greenFinanceLswl;
    private BooleanModelField greenFinanceWdxd;
    private BooleanModelField greenFinanceDonation;

    @Override
    public String setName() {
        return "绿色经营";
    }

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(greenFinance = new BooleanModelField("greenFinance", "开启绿色经营", false));
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 5000));
        modelFields.addField(greenFinanceLsxd = new BooleanModelField("greenFinanceLsxd", "绿色行动打卡(前3次有奖励)", false));
        modelFields.addField(greenFinanceLscg = new BooleanModelField("greenFinanceLscg", "绿色采购打卡", false));
        modelFields.addField(greenFinanceLsbg = new BooleanModelField("greenFinanceLsbg", "绿色办公打卡", false));
        modelFields.addField(greenFinanceWdxd = new BooleanModelField("greenFinanceWdxd", "绿色销售打卡", false));
        modelFields.addField(greenFinanceLswl = new BooleanModelField("greenFinanceLswl", "绿色物流打卡", false));
        modelFields.addField(greenFinanceDonation = new BooleanModelField("greenFinanceDonation", "1天内过期金币自动捐助", false));
        return modelFields;
    }

    public Boolean check() {
        return greenFinance.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            executeIntervalInt = Math.max(executeInterval.getValue(), 5000);
            String s = GreenFinanceRpcCall.greenFinanceIndex();
            try {
                JSONObject jo = new JSONObject(s);
                if (!jo.getBoolean("success")) {
                    Log.i(TAG, jo.optString("resultDesc"));
                    return;
                }
                JSONObject result = jo.getJSONObject("result");
                if (!result.getBoolean("greenFinanceSigned")) {
                    Log.other("绿色经营📊未开通");
                    return;
                }
                JSONObject mcaGreenLeafResult = result.getJSONObject("mcaGreenLeafResult");
                JSONArray greenLeafList = mcaGreenLeafResult.getJSONArray("greenLeafList");
                String currentCode = "";
                JSONArray bsnIds = new JSONArray();
                for (int i = 0; i < greenLeafList.length(); i++) {
                    JSONObject greenLeaf = greenLeafList.getJSONObject(i);
                    String code = greenLeaf.getString("code");
                    if (currentCode.equals(code) || bsnIds.length() == 0) {
                        bsnIds.put(greenLeaf.getString("bsnId"));
                    } else {
                        batchSelfCollect(bsnIds);
                        bsnIds = new JSONArray();
                    }
                }
                if (bsnIds.length() > 0) {
                    batchSelfCollect(bsnIds);
                }
            } catch (Throwable th) {
                Log.i(TAG, "index err:");
                Log.printStackTrace(TAG, th);
            }

            signIn("PLAY102632271");
            signIn("PLAY102932217");
            signIn("PLAY102232206");

            //执行打卡
            behaviorTick();
            //捐助
            donation();
            //绿色经营
            GreenFinanceRpcCall.doTask("AP13159535", TAG, "绿色经营📊");
        };
    }

    /**
     * 批量收取
     *
     * @param bsnIds Ids
     */
    private void batchSelfCollect(JSONArray bsnIds) {
        String s = GreenFinanceRpcCall.batchSelfCollect(bsnIds);
        try {
            JSONObject joSelfCollect = new JSONObject(s);
            if (joSelfCollect.getBoolean("success")) {
                int totalCollectPoint = joSelfCollect.getJSONObject("result").getInt("totalCollectPoint");
                Log.other("绿色经营📊收集获得" + totalCollectPoint);
            } else {
                Log.i(TAG + ".batchSelfCollect", joSelfCollect.optString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchSelfCollect err:");
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
            String s = GreenFinanceRpcCall.signInQuery(sceneId);
            JSONObject jo = new JSONObject(s);
            if (!jo.getBoolean("success")) {
                Log.i(TAG + ".signIn.signInQuery", jo.optString("resultDesc"));
                return;
            }
            JSONObject result = jo.getJSONObject("result");
            if (result.getBoolean("isTodaySignin")) {
                return;
            }
            s = GreenFinanceRpcCall.signInTrigger(sceneId);
            jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                Log.other("绿色经营📊签到成功");
            } else {
                Log.i(TAG + ".signIn.signInTrigger", jo.optString("resultDesc"));
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

    /**
     * 打卡
     */
    private void behaviorTick() {
        //绿色行动
        if (greenFinanceLsxd.getValue()) {
            doTick("lsxd");
        }
        //绿色采购
        if (greenFinanceLscg.getValue()) {
            doTick("lscg");
        }
        //绿色物流
        if (greenFinanceLswl.getValue()) {
            doTick("lswl");
        }
        //绿色办公
        if (greenFinanceLsbg.getValue()) {
            doTick("lsbg");
        }
        //绿色销售
        if (greenFinanceWdxd.getValue()) {
            doTick("wdxd");
        }
    }

    /**
     * 打卡绿色行为
     */
    private void doTick(String type) {
        try {
            String str = GreenFinanceRpcCall.queryUserTickItem(type);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".doTick.queryUserTickItem", jsonObject.optString("resultDesc"));
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if ("Y".equals(jsonObject.getString("status"))) {
                    continue;
                }
                str = GreenFinanceRpcCall.submitTick(type, jsonObject.getString("behaviorCode"));
                JSONObject object = new JSONObject(str);
                if (!object.getBoolean("success") ||
                        !String.valueOf(true).equals(GreenFinanceRpcCall.getValueByPath(object, "result.result"))) {
                    Log.i(TAG + ".doTick.submitTick", object.optString("resultDesc"));
                    continue;
                }
                Log.other("绿色经营📊[" + jsonObject.getString("title") + "]打卡成功");
//                Thread.sleep(executeIntervalInt);
            }
        } catch (Throwable th) {
            Log.i(TAG, "doTick err:");
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
     * 捐助
     */
    private void donation() {
        if (!greenFinanceDonation.getValue()) {
            return;
        }
        try {
            String str = GreenFinanceRpcCall.queryExpireMcaPoint(1);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".donation.queryExpireMcaPoint", jsonObject.optString("resultDesc"));
                return;
            }
            String strAmount = WelfareCenterRpcCall.getValueByPath(jsonObject, "result.expirePoint.amount");
            if (strAmount == null || strAmount.isEmpty() || !strAmount.matches("-?\\d+(\\.\\d+)?")) {
                return;
            }
            double amount = Double.parseDouble(strAmount);
            if (amount <= 0) {
                return;
            }
            //不管是否可以捐小于非100的倍数了，，第一次捐200，最后按amount-200*n
            Log.other("绿色经营📊1天内过期的金币[" + amount + "]");
            str = GreenFinanceRpcCall.queryAllDonationProjectNew();
            jsonObject = new JSONObject(str);
            if (!jsonObject.getBoolean("success")) {
                Log.i(TAG + ".donation.queryAllDonationProjectNew", jsonObject.optString("resultDesc"));
                return;
            }
            JSONArray result = jsonObject.getJSONArray("result");
            TreeMap<String, String> dicId = new TreeMap<>();
            for (int i = 0; i < result.length(); i++) {
                jsonObject = result.getJSONObject(i);
                str = GreenFinanceRpcCall.getValueByPath(jsonObject, "mcaDonationProjectResult.[0]");
                if (str == null || str.isEmpty()) {
                    continue;
                }
                jsonObject = new JSONObject(str);
                String pId = jsonObject.optString("projectId");
                if (pId.isEmpty()) {
                    continue;
                }
                dicId.put(pId, jsonObject.optString("projectName"));
            }
            int[] r = calculateDeductions((int) amount, dicId.size());
            String am = "200";
            for (int i = 0; i < r[0]; i++) {
                String id = new ArrayList<>(dicId.keySet()).get(i);
                String name = dicId.get(id);
                if (i == r[0] - 1) {
                    am = String.valueOf(r[1]);
                }
                str = GreenFinanceRpcCall.donation(id, am);
                jsonObject = new JSONObject(str);
                if (!jsonObject.getBoolean("success")) {
                    Log.i(TAG + ".donation." + id, jsonObject.optString("resultDesc"));
                    return;
                }
                Log.other("绿色经营📊成功捐助[" + name + "]" + am + "金币");
            }
        } catch (Throwable th) {
            Log.i(TAG, "donation err:");
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
     * 计算次数和金额
     *
     * @param amount        最小金额
     * @param maxDeductions 最大次数
     * @return [次数，最后一次的金额]
     */
    private int[] calculateDeductions(int amount, int maxDeductions) {
        if (amount < 200) {
            return new int[]{1, 200}; // 小于 200 时特殊处理
        }
        int actualDeductions = Math.min(maxDeductions, (int) Math.ceil((double) (amount) / 200)); // 实际扣款次数，不能超过最大次数
        int remainingAmount = amount - actualDeductions * 200; // 剩余金额
        // 调整剩余金额为 100 的倍数，且不小于 200
        if (remainingAmount % 100 != 0) {
            remainingAmount = ((remainingAmount + 99) / 100) * 100; // 向上取整到最近的 100 倍数
        }
        if (remainingAmount < 200) {
            remainingAmount = 200;
        }
        // 如果调整后的剩余金额需要扣除更多次数，则调整实际扣款次数
        if (remainingAmount < amount - actualDeductions * 200) {
            actualDeductions = (amount - remainingAmount) / 200;
        }
        return new int[]{actualDeductions, remainingAmount};
    }
}