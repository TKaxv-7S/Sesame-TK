package pansong291.xposed.quickenergy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import java.util.Map;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ConfigV2;
import pansong291.xposed.quickenergy.data.ModelConfig;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.data.ModelFields;
import pansong291.xposed.quickenergy.data.ViewAppInfo;
import pansong291.xposed.quickenergy.data.modelFieldExt.IntegerModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.ListModelField;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.util.BeachIdMap;
import pansong291.xposed.quickenergy.util.CooperationIdMap;
import pansong291.xposed.quickenergy.util.LanguageUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.ReserveIdMap;
import pansong291.xposed.quickenergy.util.UserIdMap;

public class SettingsActivity extends Activity {

    private Boolean isDraw = false;
    private Context context;
    private TabHost tabHost;
    private ScrollView svTabs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(getText(R.string.settings) + " " + ViewAppInfo.getAppVersion());

        context = this;
        tabHost = findViewById(R.id.tab_settings);
        svTabs = findViewById(R.id.sv_tabs);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("base")
                .setIndicator(getString(R.string.base_configuration))
                .setContent(R.id.tab_base));

        ConfigV2 config = ConfigV2.load();

        Switch sw_immediateEffect = findViewById(R.id.sw_immediateEffect);
        sw_immediateEffect.setChecked(config.isImmediateEffect());
        sw_immediateEffect.setOnClickListener(v -> config.setImmediateEffect(((Switch) v).isChecked()));
        Switch sw_recordLog = findViewById(R.id.sw_recordLog);
        sw_recordLog.setChecked(config.isRecordLog());
        sw_recordLog.setOnClickListener(v -> config.setRecordLog(((Switch) v).isChecked()));
        Switch sw_showToast = findViewById(R.id.sw_showToast);
        sw_showToast.setChecked(config.isShowToast());
        sw_showToast.setOnClickListener(v -> config.setShowToast(((Switch) v).isChecked()));
        Switch sw_stayAwake = findViewById(R.id.sw_stayAwake);
        sw_stayAwake.setChecked(config.isStayAwake());
        sw_stayAwake.setOnClickListener(v -> config.setStayAwake(((Switch) v).isChecked()));
        Switch sw_timeoutRestart = findViewById(R.id.sw_timeoutRestart);
        sw_timeoutRestart.setChecked(config.isTimeoutRestart());
        sw_timeoutRestart.setOnClickListener(v -> config.setTimeoutRestart(((Switch) v).isChecked()));
        Switch sw_enableOnGoing = findViewById(R.id.sw_enableOnGoing);
        sw_enableOnGoing.setChecked(config.isEnableOnGoing());
        sw_enableOnGoing.setOnClickListener(v -> config.setEnableOnGoing(((Switch) v).isChecked()));
        Switch sw_batteryPerm = findViewById(R.id.sw_batteryPerm);
        sw_batteryPerm.setChecked(config.isBatteryPerm());
        sw_batteryPerm.setOnClickListener(v -> config.setBatteryPerm(((Switch) v).isChecked()));
        Switch sw_newRpc = findViewById(R.id.sw_newRpc);
        sw_newRpc.setChecked(config.isNewRpc());
        sw_newRpc.setOnClickListener(v -> config.setNewRpc(((Switch) v).isChecked()));
        Switch sw_debugMode = findViewById(R.id.sw_debugMode);
        sw_debugMode.setChecked(config.isDebugMode());
        sw_debugMode.setOnClickListener(v -> config.setDebugMode(((Switch) v).isChecked()));
        Switch sw_languageSimplifiedChinese = findViewById(R.id.sw_languageSimplifiedChinese);
        sw_languageSimplifiedChinese.setChecked(config.isLanguageSimplifiedChinese());
        sw_languageSimplifiedChinese.setOnClickListener(v -> {
            config.setLanguageSimplifiedChinese(((Switch) v).isChecked());
            // 提示需要重启 language_simplified_chinese_need_restart
            Toast.makeText(this, R.string.language_simplified_chinese_need_restart, Toast.LENGTH_SHORT).show();
        });
        Button btn_execAtTimeList = findViewById(R.id.btn_execAtTimeList);
        btn_execAtTimeList.setOnClickListener(v -> StringDialog.showEditDialog(this, ((Button) v).getText(), new ListModelField.ListJoinCommaToStringModelField(config.getExecAtTimeList(), ConfigV2.DEFAULT_EXEC_AT_TIME_LIST) {
            @Override
            public void setConfigValue(String value) {
                super.setConfigValue(value);
                config.setExecAtTimeList(getValue());
            }

        }));
        Button btn_wakenAtTimeList = findViewById(R.id.btn_wakenAtTimeList);
        btn_wakenAtTimeList.setOnClickListener(v -> StringDialog.showEditDialog(this, ((Button) v).getText(), new ListModelField.ListJoinCommaToStringModelField(config.getWakenAtTimeList(), ConfigV2.DEFAULT_WAKEN_AT_TIME_LIST) {
            @Override
            public void setConfigValue(String value) {
                super.setConfigValue(value);
                config.setWakenAtTimeList(getValue());
            }

        }));
        Button btn_toastOffsetY = findViewById(R.id.btn_toastOffsetY);
        btn_toastOffsetY.setOnClickListener(v -> StringDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField(config.getToastOffsetY()) {
            @Override
            public void setConfigValue(String value) {
                super.setConfigValue(value);
                config.setToastOffsetY(getValue());
            }

        }));
        Button btn_checkInterval = findViewById(R.id.btn_checkInterval);
        btn_checkInterval.setOnClickListener(v -> StringDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField(config.getCheckInterval()) {
            @Override
            public void setConfigValue(String value) {
                super.setConfigValue(value);
                config.setCheckInterval(getValue() * 60_000);
            }

            @Override
            public String getConfigValue() {
                setValue(getValue() / 60_000);
                return super.getConfigValue();
            }
        }));
        Button btn_waitWhenException = findViewById(R.id.btn_waitWhenException);
        btn_waitWhenException.setOnClickListener(v -> StringDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField(config.getWaitWhenException()) {
            @Override
            public void setConfigValue(String value) {
                super.setConfigValue(value);
                config.setWaitWhenException(getValue() * 60_000);
            }

            @Override
            public String getConfigValue() {
                setValue(getValue() / 60_000);
                return super.getConfigValue();
            }
        }));

        Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
        for (Map.Entry<String, ModelConfig> configEntry : modelConfigMap.entrySet()) {
            String modelCode = configEntry.getKey();
            ModelConfig modelConfig = configEntry.getValue();
            ModelFields modelFields = modelConfig.getFields();

            tabHost.addTab(tabHost.newTabSpec(modelCode)
                    .setIndicator(modelConfig.getName())
                    .setContent(new TabHost.TabContentFactory() {
                        @Override
                        public View createTabContent(String tag) {
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            for (ModelField modelField : modelFields.values()) {
                                View view = modelField.getView(context);
                                if (view != null) {
                                    linearLayout.addView(view);
                                }
                            }
                            return linearLayout;
                        }
                    })
            );

        }
        tabHost.setCurrentTab(0);

        UserIdMap.shouldReload = true;
        CooperationIdMap.shouldReload = true;
        ReserveIdMap.shouldReload = true;
        BeachIdMap.shouldReload = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isDraw && hasFocus) {
            int width = svTabs.getWidth();
            TabWidget tabWidget = tabHost.getTabWidget();
            int childCount = tabWidget.getChildCount();
            for (int i = 0; i < childCount; i++) {
                tabWidget.getChildAt(i).getLayoutParams().width = width;
            }
            tabWidget.requestLayout();
            isDraw = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ConfigV2.isModify() && ConfigV2.save(false)) {
            Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
            try {
                sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.restart"));
            } catch (Throwable th) {
                Log.printStackTrace(th);
            }
        }
        UserIdMap.saveIdMap();
        CooperationIdMap.saveIdMap();
        ReserveIdMap.saveIdMap();
        BeachIdMap.saveIdMap();
    }

}
