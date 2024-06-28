package tkaxv7s.xposed.sesame.model.normal.base;

import lombok.Getter;
import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ListModelField;
import tkaxv7s.xposed.sesame.util.ListUtil;

/**
 * 基础配置模块
 */
public class BaseModel extends Model {

    @Getter
    private static final BooleanModelField stayAwake = new BooleanModelField("stayAwake", "保持唤醒", true);
    @Getter
    private static final IntegerModelField checkInterval = new IntegerModelField.MultiplyIntegerModelField("checkInterval", "执行间隔(分钟)", 30 * 60_000, 60_000, 12 * 60 * 60_000, 60_000);
    @Getter
    private static final ListModelField.ListJoinCommaToStringModelField execAtTimeList = new ListModelField.ListJoinCommaToStringModelField("execAtTimeList", "定时执行(关闭:-1)", ListUtil.newArrayList("065530", "2359", "24"));
    @Getter
    private static final ListModelField.ListJoinCommaToStringModelField wakenAtTimeList = new ListModelField.ListJoinCommaToStringModelField("wakenAtTimeList", "定时唤醒(关闭:-1)", ListUtil.newArrayList("0650", "2350"));
    @Getter
    private static final BooleanModelField timeoutRestart = new BooleanModelField("timeoutRestart", "超时重启", true);
    @Getter
    private static final IntegerModelField waitWhenException = new IntegerModelField.MultiplyIntegerModelField("waitWhenException", "异常等待时间(分钟)", 60 * 60_000, 0, 24 * 60 * 60_000, 60_000);
    @Getter
    private static final BooleanModelField batteryPerm = new BooleanModelField("batteryPerm", "为支付宝申请后台运行权限", true);
    @Getter
    private static final BooleanModelField newRpc = new BooleanModelField("newRpc", "使用新接口(最低支持v10.3.96.8100)", true);
    @Getter
    private static final BooleanModelField debugMode = new BooleanModelField("debugMode", "开启抓包(基于新接口)", false);
    @Getter
    private static final BooleanModelField recordLog = new BooleanModelField("recordLog", "记录日志", true);
    @Getter
    private static final BooleanModelField enableOnGoing = new BooleanModelField("enableOnGoing", "开启状态栏禁删", false);
    @Getter
    private static final BooleanModelField languageSimplifiedChinese = new BooleanModelField("languageSimplifiedChinese", "只显示中文并设置时区", true);
    @Getter
    private static final BooleanModelField showToast = new BooleanModelField("showToast", "气泡提示", true);
    @Getter
    private static final IntegerModelField toastOffsetY = new IntegerModelField("toastOffsetY", "气泡纵向偏移", 0);

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

}
