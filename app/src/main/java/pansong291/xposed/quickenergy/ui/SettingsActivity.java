package pansong291.xposed.quickenergy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ConfigV2;
import pansong291.xposed.quickenergy.data.ModelConfig;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.data.ModelFields;
import pansong291.xposed.quickenergy.data.modelFieldExt.BooleanModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.ChoiceModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.IdAndNameSelectModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.IntegerModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.StringModelField;
import pansong291.xposed.quickenergy.entity.IdAndName;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.util.BeachIdMap;
import pansong291.xposed.quickenergy.util.CooperationIdMap;
import pansong291.xposed.quickenergy.util.LanguageUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.ReserveIdMap;
import pansong291.xposed.quickenergy.util.UserIdMap;

public class SettingsActivity extends Activity {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int MAX_TAB_INDEX = 3;

    private Context context;
    private TabHost tabHost;
    private ScrollView svTabs;
    private GestureDetector gestureDetector;
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings);

        context = this;
        tabHost = findViewById(R.id.tab_settings);
        svTabs = findViewById(R.id.sv_tabs);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("base")
                .setIndicator(getString(R.string.base_configuration))
                .setContent(R.id.tab_base));

        ConfigV2 config = ConfigV2.load(context);

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
        Switch sw_startAt0 = findViewById(R.id.sw_startAt0);
        sw_startAt0.setChecked(config.isStartAt0());
        sw_startAt0.setOnClickListener(v -> config.setStartAt0(((Switch) v).isChecked()));
        Switch sw_startAt7 = findViewById(R.id.sw_startAt7);
        sw_startAt7.setChecked(config.isStartAt7());
        sw_startAt7.setOnClickListener(v -> config.setStartAt7(((Switch) v).isChecked()));
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
        Button btn_toastOffsetY = findViewById(R.id.btn_toastOffsetY);
        btn_toastOffsetY.setOnClickListener(v -> {
            EditDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField() {
                @Override
                public void setValue(Object value) {
                    super.setValue(value);
                    config.setToastOffsetY(super.getValue());
                }

                @Override
                public Integer getValue() {
                    return config.getToastOffsetY();
                }
            });
        });
        Button btn_checkInterval = findViewById(R.id.btn_checkInterval);
        btn_checkInterval.setOnClickListener(v -> {
            EditDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField() {
                @Override
                public void setValue(Object value) {
                    super.setValue(value);
                    config.setCheckInterval(super.getValue() * 60_000);
                }

                @Override
                public Integer getValue() {
                    return config.getCheckInterval() / 60_000;
                }
            });
        });
        Button btn_waitWhenException = findViewById(R.id.btn_waitWhenException);
        btn_waitWhenException.setOnClickListener(v -> {
            EditDialog.showEditDialog(this, ((Button) v).getText(), new IntegerModelField() {
                @Override
                public void setValue(Object value) {
                    super.setValue(value);
                    config.setWaitWhenException(super.getValue() * 60 * 1000);
                }

                @Override
                public Integer getValue() {
                    return config.getWaitWhenException() / 60 / 1000;
                }
            });
        });

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
                            for (Map.Entry<String, ModelField> fieldEntry : modelFields.entrySet()) {
                                String fieldCode = fieldEntry.getKey();
                                ModelField modelField = fieldEntry.getValue();
                                if (modelField instanceof BooleanModelField) {
                                    BooleanModelField booleanModelField = (BooleanModelField) modelField;
                                    Switch sw = new Switch(context);
                                    sw.setText(booleanModelField.getName());
                                    sw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    sw.setMinHeight(150);
                                    sw.setPaddingRelative(40, 0, 40, 0);
                                    final BooleanModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    if (configModelField != null) {
                                        sw.setChecked(configModelField.getValue());
                                    }
                                    sw.setOnClickListener(v -> {
                                        BooleanModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        thisModelField.setValue(((Switch) v).isChecked());
                                    });
                                    linearLayout.addView(sw);
                                } else if (modelField instanceof IntegerModelField) {
                                    IntegerModelField integerModelField = (IntegerModelField) modelField;
                                    Button btn = new Button(context);
                                    btn.setText(integerModelField.getName());
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    btn.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
                                    btn.setBackground(getResources().getDrawable(R.drawable.button));
                                    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                                    btn.setMinHeight(150);
                                    btn.setPaddingRelative(40, 0, 40, 0);
                                    btn.setAllCaps(false);
                                    final IntegerModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    btn.setOnClickListener(v -> {
                                        IntegerModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        IntegerModelField finalThisModelField = thisModelField;
                                        EditDialog.showEditDialog(v.getContext(), ((Button) v).getText(), new IntegerModelField() {
                                            @Override
                                            public void setValue(Object value) {
                                                finalThisModelField.setConfigValue(value);
                                            }

                                            @Override
                                            public Integer getValue() {
                                                return finalThisModelField.getConfigValue();
                                            }
                                        });
                                    });
                                    linearLayout.addView(btn);
                                } else if (modelField instanceof StringModelField) {
                                    StringModelField stringModelField = (StringModelField) modelField;
                                    Button btn = new Button(context);
                                    btn.setText(stringModelField.getName());
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    btn.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
                                    btn.setBackground(getResources().getDrawable(R.drawable.button));
                                    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                                    btn.setMinHeight(150);
                                    btn.setPaddingRelative(40, 0, 40, 0);
                                    btn.setAllCaps(false);
                                    final StringModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    btn.setOnClickListener(v -> {
                                        StringModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        StringModelField finalThisModelField = thisModelField;
                                        EditDialog.showEditDialog(v.getContext(), ((Button) v).getText(), new StringModelField() {
                                            @Override
                                            public void setValue(Object value) {
                                                finalThisModelField.setValue(value);
                                            }

                                            @Override
                                            public String getValue() {
                                                return finalThisModelField.getValue();
                                            }
                                        });
                                    });
                                    linearLayout.addView(btn);
                                } else if (modelField instanceof IdAndNameSelectModelField.UserAndNameSelectOneModelField) {
                                    IdAndNameSelectModelField.UserAndNameSelectOneModelField userAndNameSelectOneModelField = (IdAndNameSelectModelField.UserAndNameSelectOneModelField) modelField;
                                    Button btn = new Button(context);
                                    btn.setText(userAndNameSelectOneModelField.getName());
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    btn.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
                                    btn.setBackground(getResources().getDrawable(R.drawable.button));
                                    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                                    btn.setMinHeight(150);
                                    btn.setPaddingRelative(40, 0, 40, 0);
                                    btn.setAllCaps(false);
                                    final IdAndNameSelectModelField.UserAndNameSelectOneModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    btn.setOnClickListener(v -> {
                                        IdAndNameSelectModelField.UserAndNameSelectOneModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        IdAndNameSelectModelField.UserAndNameSelectOneModelField finalThisModelField = thisModelField;
                                        ListDialog.show(v.getContext(), ((Button) v).getText(), new IdAndNameSelectModelField() {
                                            @Override
                                            public List<? extends IdAndName> getList() {
                                                return finalThisModelField.getList();
                                            }

                                            @Override
                                            public void setValue(Object value) {
                                                finalThisModelField.setValue(value);
                                            }

                                            @Override
                                            public IdAndNameSelectModelField.KVNode<List<String>, List<Integer>> getValue() {
                                                return finalThisModelField.getValue();
                                            }
                                        }, ListDialog.ListType.RADIO);
                                    });
                                    linearLayout.addView(btn);
                                } else if (modelField instanceof IdAndNameSelectModelField) {
                                    IdAndNameSelectModelField idAndNameSelectModelField = (IdAndNameSelectModelField) modelField;
                                    Button btn = new Button(context);
                                    btn.setText(idAndNameSelectModelField.getName());
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    btn.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
                                    btn.setBackground(getResources().getDrawable(R.drawable.button));
                                    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                                    btn.setMinHeight(150);
                                    btn.setPaddingRelative(40, 0, 40, 0);
                                    btn.setAllCaps(false);
                                    final IdAndNameSelectModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    btn.setOnClickListener(v -> {
                                        IdAndNameSelectModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        IdAndNameSelectModelField finalThisModelField = thisModelField;
                                        ListDialog.show(v.getContext(), ((Button) v).getText(), new IdAndNameSelectModelField() {
                                            @Override
                                            public List<? extends IdAndName> getList() {
                                                return finalThisModelField.getList();
                                            }

                                            @Override
                                            public void setValue(Object value) {
                                                finalThisModelField.setValue(value);
                                            }

                                            @Override
                                            public IdAndNameSelectModelField.KVNode<List<String>, List<Integer>> getValue() {
                                                return finalThisModelField.getValue();
                                            }
                                        });
                                    });
                                    linearLayout.addView(btn);
                                } else if (modelField instanceof ChoiceModelField) {
                                    ChoiceModelField choiceModelField = (ChoiceModelField) modelField;
                                    Button btn = new Button(context);
                                    btn.setText(choiceModelField.getName());
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    btn.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
                                    btn.setBackground(getResources().getDrawable(R.drawable.button));
                                    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                                    btn.setMinHeight(150);
                                    btn.setPaddingRelative(40, 0, 40, 0);
                                    btn.setAllCaps(false);
                                    final ChoiceModelField configModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                    btn.setOnClickListener(v -> {
                                        ChoiceModelField thisModelField = configModelField;
                                        if (thisModelField == null) {
                                            thisModelField = ConfigV2.INSTANCE.getModelFieldExt(modelCode, fieldCode, true);
                                            if (thisModelField == null) {
                                                Log.i("无法获取配置字段 modelCode:" + modelCode + " fieldCode:" + fieldCode);
                                                return;
                                            }
                                        }
                                        ChoiceModelField finalThisModelField = thisModelField;
                                        ChoiceDialog.show(v.getContext(), ((Button) v).getText(), new ChoiceModelField() {
                                            @Override
                                            public CharSequence[] getList() {
                                                return finalThisModelField.getList();
                                            }

                                            @Override
                                            public void setValue(Object value) {
                                                finalThisModelField.setValue(value);
                                            }

                                            @Override
                                            public Integer getValue() {
                                                return finalThisModelField.getValue();
                                            }
                                        });
                                    });
                                    linearLayout.addView(btn);
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
        if (hasFocus) {
            int width = svTabs.getWidth();
            TabWidget tabWidget = tabHost.getTabWidget();
            int childCount = tabWidget.getChildCount();
            for (int i = 0; i < childCount; i++) {
                tabWidget.getChildAt(i).getLayoutParams().width = width;
            }
            tabWidget.requestLayout();
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
