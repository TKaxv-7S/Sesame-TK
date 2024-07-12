package tkaxv7s.xposed.sesame.model.normal.base;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ListModelField;
import tkaxv7s.xposed.sesame.model.task.antOcean.AntOceanRpcCall;
import tkaxv7s.xposed.sesame.model.task.reserve.ReserveRpcCall;
import tkaxv7s.xposed.sesame.util.*;

/**
 * 基础配置模块
 */
public class BaseModel extends Model {

    @Getter
    private static final BooleanModelField stayAwake = new BooleanModelField("stayAwake", "保持唤醒", true);
    @Getter
    private static final IntegerModelField.MultiplyIntegerModelField checkInterval = new IntegerModelField.MultiplyIntegerModelField("checkInterval", "执行间隔(分钟)", 50, 1, 12 * 60, 60_000);
    @Getter
    private static final ListModelField.ListJoinCommaToStringModelField execAtTimeList = new ListModelField.ListJoinCommaToStringModelField("execAtTimeList", "定时执行(关闭:-1)", ListUtil.newArrayList("065530", "2359", "24"));
    @Getter
    private static final ListModelField.ListJoinCommaToStringModelField wakenAtTimeList = new ListModelField.ListJoinCommaToStringModelField("wakenAtTimeList", "定时唤醒(关闭:-1)", ListUtil.newArrayList("0650", "2350"));
    @Getter
    private static final ListModelField.ListJoinCommaToStringModelField energyTime = new ListModelField.ListJoinCommaToStringModelField("energyTime", "只收能量时间(范围)", ListUtil.newArrayList("0700-0731"));
    @Getter
    private static final ChoiceModelField timedTaskModel = new ChoiceModelField("timedTaskModel", "定时任务模式", TimedTaskModel.SYSTEM, TimedTaskModel.nickNames);
    @Getter
    private static final BooleanModelField timeoutRestart = new BooleanModelField("timeoutRestart", "超时重启", true);
    @Getter
    private static final IntegerModelField.MultiplyIntegerModelField waitWhenException = new IntegerModelField.MultiplyIntegerModelField("waitWhenException", "异常等待时间(分钟)", 60, 0, 24 * 60, 60_000);
    @Getter
    private static final BooleanModelField newRpc = new BooleanModelField("newRpc", "使用新接口(最低支持v10.3.96.8100)", true);
    @Getter
    private static final BooleanModelField debugMode = new BooleanModelField("debugMode", "开启抓包(基于新接口)", false);
    @Getter
    private static final BooleanModelField batteryPerm = new BooleanModelField("batteryPerm", "为支付宝申请后台运行权限", true);
    @Getter
    private static final BooleanModelField recordLog = new BooleanModelField("recordLog", "记录日志", true);
    @Getter
    private static final BooleanModelField showToast = new BooleanModelField("showToast", "气泡提示", true);
    @Getter
    private static final IntegerModelField toastOffsetY = new IntegerModelField("toastOffsetY", "气泡纵向偏移", 0);
    @Getter
    private static final BooleanModelField languageSimplifiedChinese = new BooleanModelField("languageSimplifiedChinese", "只显示中文并设置时区", true);
    @Getter
    private static final BooleanModelField enableOnGoing = new BooleanModelField("enableOnGoing", "开启状态栏禁删", false);

    @Override
    public String getName() {
        return "基础";
    }

    @Override
    public String getEnableFieldName() {
        return "启用模块";
    }

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(stayAwake);
        modelFields.addField(checkInterval);
        modelFields.addField(execAtTimeList);
        modelFields.addField(wakenAtTimeList);
        modelFields.addField(energyTime);
        modelFields.addField(timedTaskModel);
        modelFields.addField(timeoutRestart);
        modelFields.addField(waitWhenException);
        modelFields.addField(batteryPerm);
        modelFields.addField(newRpc);
        modelFields.addField(debugMode);
        modelFields.addField(recordLog);
        modelFields.addField(enableOnGoing);
        modelFields.addField(languageSimplifiedChinese);
        modelFields.addField(showToast);
        modelFields.addField(toastOffsetY);
        return modelFields;
    }

    public static void initData() {
        new Thread(() -> {
            try {
                initReserve();
                initBeach();
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }).start();
    }

    public static void destroyData() {
        try {
            ReserveIdMap.clear();
            BeachIdMap.clear();
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    private static void initReserve() {
        try {
            String s = ReserveRpcCall.queryTreeItemsForExchange();
            if (s == null) {
                Thread.sleep(RandomUtil.delay());
                s = ReserveRpcCall.queryTreeItemsForExchange();
            }
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("treeItems");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("projectType"))
                        continue;
                    if (!"RESERVE".equals(jo.getString("projectType"))) {
                        continue;
                    }
                    if (!"AVAILABLE".equals(jo.getString("applyAction"))) {
                        continue;
                    }
                    ReserveIdMap.add(jo.getString("itemId"), jo.getString("itemName") + "(" + jo.getInt("energy") + "g)");
                }
                ReserveIdMap.save();
            } else {
                Log.i(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(t);
            ReserveIdMap.load();
        }
    }

    private static void initBeach() {
        try {
            String s = AntOceanRpcCall.queryCultivationList();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("cultivationItemVOList");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("templateSubType")) {
                        continue;
                    }
                    if (!"BEACH".equals(jo.getString("templateSubType"))
                            && !"COOPERATE_SEA_TREE".equals(jo.getString("templateSubType")) && !"SEA_ANIMAL".equals(jo.getString("templateSubType"))) {
                        continue;
                    }
                    if (!"AVAILABLE".equals(jo.getString("applyAction"))) {
                        continue;
                    }
                    BeachIdMap.add(jo.getString("templateCode"), jo.getString("cultivationName") + "(" + jo.getInt("energy") + "g)");
                }
                BeachIdMap.save();
            } else {
                Log.i(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(t);
            BeachIdMap.load();
        }
    }

    public interface TimedTaskModel {

        int SYSTEM = 0;

        int PROGRAM = 1;

        String[] nickNames = {"系统计时", "程序计时"};

    }

}
