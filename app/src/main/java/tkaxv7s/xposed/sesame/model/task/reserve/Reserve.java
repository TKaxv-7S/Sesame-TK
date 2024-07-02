package tkaxv7s.xposed.sesame.model.task.reserve;

import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.entity.AlipayReserve;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.RandomUtil;
import tkaxv7s.xposed.sesame.util.Status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Reserve extends ModelTask {
    private static final String TAG = Reserve.class.getSimpleName();

    @Override
    public String getName() {
        return "保护地";
    }

    private SelectModelField reserveList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(reserveList = new SelectModelField("reserveList", "保护地列表", new KVNode<>(new LinkedHashMap<>(), true), AlipayReserve.getList()));
        return modelFields;
    }

    public Boolean check() {
        if (TaskCommon.IS_ENERGY_TIME) {
            return false;
        }
        return true;
    }

    public void run() {
        try {
            Log.record("开始检测保护地");
            animalReserve();
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void animalReserve() {
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
                    if (!jo.has("projectType")) {
                        continue;
                    }
                    if (!"RESERVE".equals(jo.getString("projectType"))) {
                        continue;
                    }
                    if (!"AVAILABLE".equals(jo.getString("applyAction"))) {
                        continue;
                    }
                    String projectId = jo.getString("itemId");
                    String itemName = jo.getString("itemName");
                    Map<String, Integer> map = reserveList.getValue().getKey();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        if (Objects.equals(entry.getKey(), projectId)) {
                            Integer count = entry.getValue();
                            if (count != null && count > 0 && Status.canReserveToday(projectId, count)) {
                                exchangeTree(projectId, itemName, count);
                            }
                            break;
                        }
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "animalReserve err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private boolean queryTreeForExchange(String projectId) {
        try {
            String s = ReserveRpcCall.queryTreeForExchange(projectId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                String applyAction = jo.getString("applyAction");
                int currentEnergy = jo.getInt("currentEnergy");
                jo = jo.getJSONObject("exchangeableTree");
                if ("AVAILABLE".equals(applyAction)) {
                    if (currentEnergy >= jo.getInt("energy")) {
                        return true;
                    } else {
                        Log.forest("领保护地🏕️[" + jo.getString("projectName") + "]#能量不足停止申请");
                        return false;
                    }
                } else {
                    Log.forest("领保护地🏕️[" + jo.getString("projectName") + "]#似乎没有了");
                    return false;
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryTreeForExchange err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void exchangeTree(String projectId, String itemName, int count) {
        int appliedTimes = 0;
        try {
            String s;
            JSONObject jo;
            boolean canApply = queryTreeForExchange(projectId);
            if (!canApply)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                s = ReserveRpcCall.exchangeTree(projectId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    int vitalityAmount = jo.optInt("vitalityAmount", 0);
                    appliedTimes = Status.getReserveTimes(projectId) + 1;
                    String str = "领保护地🏕️[" + itemName + "]#第" + appliedTimes + "次"
                            + (vitalityAmount > 0 ? "-活力值+" + vitalityAmount : "");
                    Log.forest(str);
                    Status.reserveToday(projectId, 1);
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.toString());
                    Log.forest("领保护地🏕️[" + itemName + "]#发生未知错误，停止申请");
                    // Statistics.reserveToday(projectId, count);
                    break;
                }
                Thread.sleep(300);
                canApply = queryTreeForExchange(projectId);
                if (!canApply) {
                    // Statistics.reserveToday(projectId, count);
                    break;
                } else {
                    Thread.sleep(300);
                }
                if (!Status.canReserveToday(projectId, count))
                    break;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
