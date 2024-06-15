package pansong291.xposed.quickenergy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private GestureDetector gestureDetector;
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings);

        context = this;
        tabHost = findViewById(R.id.tab_settings);
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
            EditDialog.showEditDialog(this, ((Button) v).getText(), new ModelField() {
                @Override
                public void setValue(Object value) {
                    config.setToastOffsetY((Integer) value);
                }

                @Override
                public Integer getValue() {
                    return config.getToastOffsetY();
                }
            });
        });
        Button btn_checkInterval = findViewById(R.id.btn_checkInterval);
        btn_checkInterval.setOnClickListener(v -> {
            EditDialog.showEditDialog(this, ((Button) v).getText(), new ModelField() {
                @Override
                public void setValue(Object value) {
                    config.setCheckInterval(((Integer) value) * 60_000);
                }

                @Override
                public Integer getValue() {
                    return config.getCheckInterval() / 60_000;
                }
            });
        });
        Button btn_waitWhenException = findViewById(R.id.btn_waitWhenException);
        btn_waitWhenException.setOnClickListener(v -> {
            EditDialog.showEditDialog(this, ((Button) v).getText(), new ModelField() {
                @Override
                public void setValue(Object value) {
                    config.setWaitWhenException(((Integer) value) * 60 * 1000);
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
                                                finalThisModelField.setConfigValue(value);
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
                                                finalThisModelField.setConfigValue(value);
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
                                                finalThisModelField.setConfigValue(value);
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
        TabWidget tabWidget = tabHost.getTabWidget();
        int childCount = tabWidget.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = tabWidget.getChildAt(i);
            child.getLayoutParams().width = 280;
        }
        tabHost.setCurrentTab(0);

        initFlipper();

        UserIdMap.shouldReload = true;
        CooperationIdMap.shouldReload = true;
        ReserveIdMap.shouldReload = true;
        BeachIdMap.shouldReload = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        sw_collectEnergy.setChecked(config.isCollectEnergy());
//        sw_collectWateringBubble.setChecked(config.isCollectWateringBubble());
//        sw_batchRobEnergy.setChecked(config.isBatchRobEnergy());
//        sw_collectProp.setChecked(config.isCollectProp());
//        sw_helpFriendCollect.setChecked(config.isHelpFriendCollect());
//        sw_receiveForestTaskAward.setChecked(config.isReceiveForestTaskAward());
//        sw_cooperateWater.setChecked(config.isCooperateWater());
//        sw_ancientTree.setChecked(config.isAncientTree());
//        sw_energyRain.setChecked(config.isEnergyRain());
//        sw_reserve.setChecked(config.isReserve());
//        sw_beach.setChecked(config.isBeach());
//        sw_enableFarm.setChecked(config.isEnableFarm());
//        sw_rewardFriend.setChecked(config.isRewardFriend());
//        sw_sendBackAnimal.setChecked(config.isSendBackAnimal());
//        sw_receiveFarmToolReward.setChecked(config.isReceiveFarmToolReward());
//        sw_recordFarmGame.setChecked(config.isRecordFarmGame());
//        sw_kitchen.setChecked(config.isKitchen());
//        sw_special_food.setChecked(config.isUseSpecialFood());
//        sw_useNewEggTool.setChecked(config.isUseNewEggTool());
//        sw_harvestProduce.setChecked(config.isHarvestProduce());
//        sw_donation.setChecked(config.isDonation());
//        sw_answerQuestion.setChecked(config.isAnswerQuestion());
//        sw_receiveFarmTaskAward.setChecked(config.isReceiveFarmTaskAward());
//        sw_feedAnimal.setChecked(config.isFeedAnimal());
//        sw_useAccelerateTool.setChecked(config.isUseAccelerateTool());
//        sw_notifyFriend.setChecked(config.isNotifyFriend());
//        sw_enableChouchoule.setChecked(config.isEnableChouchoule());
//        sw_acceptGift.setChecked(config.isAcceptGift());
//        sw_chickenDiary.setChecked(config.isChickenDiary());
//        sw_antOrchard.setChecked(config.isAntOrchard());
//        sw_receiveOrchardTaskAward.setChecked(config.isReceiveOrchardTaskAward());
//        sw_receivePoint.setChecked(config.isReceivePoint());
//        sw_openTreasureBox.setChecked(config.isOpenTreasureBox());
//        sw_receiveCoinAsset.setChecked(config.isReceiveCoinAsset());
//        sw_donateCharityCoin.setChecked(config.isDonateCharityCoin());
//        sw_kbSignIn.setChecked(config.isKbSignIn());
//        sw_limitCollect.setChecked(config.isLimitCollect());
//        sw_doubleCard.setChecked(config.isDoubleCard());
//        sw_ExchangeEnergyDoubleClick.setChecked(config.isExchangeEnergyDoubleClick());
//        sw_ecoLifeTick.setChecked(config.isEcoLifeTick());
//        sw_tiyubiz.setChecked(config.isTiyubiz());
//        sw_insBlueBeanExchange.setChecked(config.isInsBlueBeanExchange());
//        sw_collectSesame.setChecked(config.isCollectSesame());
//        sw_zcjSignIn.setChecked(config.isZcjSignIn());
//        sw_merchantKmdk.setChecked(config.isMerchantKmdk());
//        sw_ancientTreeOnlyWeek.setChecked(config.isAncientTreeOnlyWeek());
//        sw_antdodoCollect.setChecked(config.isAntdodoCollect());
//        sw_antOcean.setChecked(config.isAntOcean());
//        sw_userPatrol.setChecked(config.isUserPatrol());
//        sw_animalConsumeProp.setChecked(config.isAnimalConsumeProp());
//        sw_collectGiftBox.setChecked(config.isCollectGiftBox());
//        sw_totalCertCount.setChecked(config.isTotalCertCount());
//
//        sw_enableStall.setChecked(config.isEnableStall());
//        sw_stallAutoClose.setChecked(config.isStallAutoClose());
//        sw_stallAutoOpen.setChecked(config.isStallAutoOpen());
//        sw_stallAutoTask.setChecked(config.isStallAutoTask());
//        sw_stallReceiveAward.setChecked(config.isStallReceiveAward());
//        sw_stallOpenType.setChecked(config.isStallOpenType());
//        sw_stallDonate.setChecked(config.isStallDonate());
//        sw_stallInviteRegister.setChecked(config.isStallInviteRegister());
//        sw_stallThrowManure.setChecked(config.isStallThrowManure());
//        sw_greenFinance.setChecked(config.isGreenFinance());
        //sw_antBookRead.setChecked(config.isAntBookRead());
        //sw_consumeGold.setChecked(config.isConsumeGold());
        //sw_omegakoiTown.setChecked(config.isOmegakoiTown());
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    private void initFlipper() {
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                int lastView = tabHost.getCurrentTab();
                int currentView = lastView;
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (currentView < MAX_TAB_INDEX) {
                        currentView++;
                    }
                    setCurrentTab(lastView, currentView);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (currentView > 0) {
                        currentView--;
                    }
                    setCurrentTab(lastView, currentView);
                }
                return true;
            }
        });
    }

    private void setCurrentTab(int lastView, int currentView) {
        if (lastView == currentView)
            return;
        if (lastView < currentView) {
            tabHost.getCurrentView().startAnimation(slideLeftOut);
        } else {
            tabHost.getCurrentView().startAnimation(slideRightOut);
        }

        tabHost.setCurrentTab(currentView);

        if (lastView < currentView) {
            tabHost.getCurrentView().startAnimation(slideRightIn);
        } else {
            tabHost.getCurrentView().startAnimation(slideLeftIn);
        }
    }

    /*private void initSwitch() {

//        sw_collectEnergy = findViewById(R.id.sw_collectEnergy);
//        sw_collectWateringBubble = findViewById(R.id.sw_collectWateringBubble);
//        sw_batchRobEnergy = findViewById(R.id.sw_batchRobEnergy);
//        sw_collectProp = findViewById(R.id.sw_collectProp);
//        sw_helpFriendCollect = findViewById(R.id.sw_helpFriendCollect);
//        sw_receiveForestTaskAward = findViewById(R.id.sw_receiveForestTaskAward);
//        sw_cooperateWater = findViewById(R.id.sw_cooperateWater);
//        sw_ancientTree = findViewById(R.id.sw_ancientTree);
//        sw_energyRain = findViewById(R.id.sw_energyRain);
//        sw_reserve = findViewById(R.id.sw_reserve);
//        sw_beach = findViewById(R.id.sw_beach);
//        sw_enableFarm = findViewById(R.id.sw_enableFarm);
//        sw_rewardFriend = findViewById(R.id.sw_rewardFriend);
//        sw_sendBackAnimal = findViewById(R.id.sw_sendBackAnimal);
//        sw_receiveFarmToolReward = findViewById(R.id.sw_receiveFarmToolReward);
//        sw_recordFarmGame = findViewById(R.id.sw_recordFarmGame);
//        sw_kitchen = findViewById(R.id.sw_kitchen);
//        sw_special_food = findViewById(R.id.sw_special_food);
//        sw_useNewEggTool = findViewById(R.id.sw_useNewEggTool);
//        sw_harvestProduce = findViewById(R.id.sw_harvestProduce);
//        sw_donation = findViewById(R.id.sw_donation);
//        sw_answerQuestion = findViewById(R.id.sw_answerQuestion);
//        sw_receiveFarmTaskAward = findViewById(R.id.sw_receiveFarmTaskAward);
//        sw_feedAnimal = findViewById(R.id.sw_feedAnimal);
//        sw_useAccelerateTool = findViewById(R.id.sw_useAccelerateTool);
//        sw_enableChouchoule = findViewById(R.id.sw_enableChouchoule);
//        sw_notifyFriend = findViewById(R.id.sw_notifyFriend);
//        sw_acceptGift = findViewById(R.id.sw_acceptGift);
//        sw_chickenDiary = findViewById(R.id.sw_chickenDiary);
//        sw_antOrchard = findViewById(R.id.sw_antOrchard);
//        sw_receiveOrchardTaskAward = findViewById(R.id.sw_receiveOrchardTaskAward);
//        sw_receivePoint = findViewById(R.id.sw_receivePoint);
//        sw_openTreasureBox = findViewById(R.id.sw_openTreasureBox);
//        sw_receiveCoinAsset = findViewById(R.id.sw_receiveCoinAsset);
//        sw_donateCharityCoin = findViewById(R.id.sw_donateCharityCoin);
//        sw_kbSignIn = findViewById(R.id.sw_kbSignIn);
//        sw_limitCollect = findViewById(R.id.sw_limitCollect);
//        sw_doubleCard = findViewById(R.id.sw_doubleCard);
//        sw_ExchangeEnergyDoubleClick = findViewById(R.id.sw_ExchangeEnergyDoubleClick);
//        sw_ecoLifeTick = findViewById(R.id.sw_ecoLifeTick);
//        sw_tiyubiz = findViewById(R.id.sw_tiyubiz);
//        sw_insBlueBeanExchange = findViewById(R.id.sw_insBlueBeanExchange);
//        sw_collectSesame = findViewById(R.id.sw_collectSesame);
//        sw_zcjSignIn = findViewById(R.id.sw_zcjSignIn);
//        sw_merchantKmdk = findViewById(R.id.sw_merchantKmdk);
//        sw_ancientTreeOnlyWeek = findViewById(R.id.sw_ancientTreeOnlyWeek);
//        sw_antdodoCollect = findViewById(R.id.sw_antdodoCollect);
//        sw_antOcean = findViewById(R.id.sw_antOcean);
//        sw_userPatrol = findViewById(R.id.sw_userPatrol);
//        sw_animalConsumeProp = findViewById(R.id.sw_animalConsumeProp);
//        sw_collectGiftBox = findViewById(R.id.sw_collectGiftBox);
//        sw_totalCertCount = findViewById(R.id.sw_totalCertCount);
//
//        sw_enableStall = findViewById(R.id.sw_enableStall);
//        sw_stallAutoClose = findViewById(R.id.sw_stallAutoClose);
//        sw_stallAutoOpen = findViewById(R.id.sw_stallAutoOpen);
//        sw_stallAutoTask = findViewById(R.id.sw_stallAutoTask);
//        sw_stallReceiveAward = findViewById(R.id.sw_stallReceiveAward);
//        sw_stallOpenType = findViewById(R.id.sw_stallOpenType);
//        sw_stallDonate = findViewById(R.id.sw_stallDonate);
//        sw_stallInviteRegister = findViewById(R.id.sw_stallInviteRegister);
//        sw_stallThrowManure = findViewById(R.id.sw_stallThrowManure);
//        sw_greenFinance = findViewById(R.id.sw_greenFinance);
        //sw_antBookRead = findViewById(R.id.sw_antBookRead);
        //sw_consumeGold = findViewById(R.id.sw_consumeGold);
        //sw_omegakoiTown = findViewById(R.id.sw_omegakoiTown);
    }*/

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (v instanceof Button) {
            Button btn = (Button) v;
            switch (v.getId()) {

                /*case R.id.btn_advanceTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.ADVANCE_TIME);
                    break;

                case R.id.btn_collectInterval:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.COLLECT_INTERVAL);
                    break;

                case R.id.btn_collectTimeout:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.COLLECT_TIMEOUT);
                    break;

                case R.id.btn_limitCount:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.LIMIT_COUNT);
                    break;

                case R.id.btn_doubleCardTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.DOUBLE_CARD_TIME,
                            this.getString(R.string.use_double_card_time_desc));
                    break;

                case R.id.btn_doubleCountLimit:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.DOUBLE_COUNT_LIMIT);
                    break;

                case R.id.btn_returnWater30:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.RETURN_WATER_30);
                    break;

                case R.id.btn_returnWater20:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.RETURN_WATER_20);
                    break;

                case R.id.btn_returnWater10:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.RETURN_WATER_10);
                    break;

                case R.id.btn_dontCollectList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getDontCollectList(), null);
                    break;

                case R.id.btn_dontHelpCollectList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getDontHelpCollectList(), null);
                    break;

                case R.id.btn_waterFriendList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getWaterFriendList(),
                            config.getWaterCountList());
                    break;

                case R.id.btn_waterFriendCount:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.WATER_FRIEND_COUNT);
                    break;

                case R.id.btn_cooperateWaterList:
                    ListDialog.show(this, btn.getText(), CooperateUser.getList(), config.getCooperateWaterList(),
                            config.getCooperateWaterNumList());
                    break;

                case R.id.btn_ancientTreeAreaCodeList:
                    ListDialog.show(this, btn.getText(), AreaCode.getList(), config.getAncientTreeCityCodeList(), null);
                    break;

                case R.id.btn_giveEnergyRainList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getGiveEnergyRainList(), null);
                    break;

                case R.id.btn_reserveList:
                    ListDialog.show(this, btn.getText(), AlipayReserve.getList(), config.getReserveList(),
                            config.getReserveCountList());
                    break;

                case R.id.btn_beachList:
                    ListDialog.show(this, btn.getText(), AlipayBeach.getList(), config.getBeachList(),
                            config.getBeachCountList());
                    break;

                case R.id.btn_dontSendFriendList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getDontSendFriendList(), null);
                    break;

                case R.id.btn_farmGameTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.FARM_GAME_TIME);
                    break;

                case R.id.btn_feedFriendAnimalList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getFeedFriendAnimalList(),
                            config.getFeedFriendCountList());
                    break;

                case R.id.btn_dontNotifyFriendList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getDontNotifyFriendList(), null);
                    break;

                case R.id.btn_visitFriendList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getVisitFriendList(),
                            config.getVisitFriendCountList());
                    break;

                case R.id.btn_animalSleepTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.ANIMAL_SLEEP_TIME);
                    break;

                case R.id.btn_minExchangeCount:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.MIN_EXCHANGE_COUNT);
                    break;

                case R.id.btn_latestExchangeTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.LATEST_EXCHANGE_TIME);
                    break;

                case R.id.btn_syncStepCount:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.SYNC_STEP_COUNT);
                    break;

                case R.id.btn_ExchangeEnergyDoubleClickCount:
                    EditDialog.showEditDialog(this, btn.getText(),
                            EditDialog.EditMode.EXCHANGE_ENERGY_DOUBLE_CLICK_COUNT);
                    break;

                case R.id.btn_WhoYouWantToGiveTo:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getWhoYouWantGiveTo(), null,
                            ListDialog.ListType.RADIO);
                    break;

                case R.id.btn_sendFriendCard:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getSendFriendCard(), null,
                            ListDialog.ListType.RADIO);
                    break;

                case R.id.btn_orchardSpreadManureCount:
                    EditDialog.showEditDialog(this, btn.getText(),
                            EditDialog.EditMode.ORCHARD_SPREAD_MANURE_COUNT);
                    break;

                case R.id.btn_stallOpenList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getStallOpenList(), null);
                    break;

                case R.id.btn_stallWhiteList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getStallWhiteList(), null);
                    break;

                case R.id.btn_stallBlackList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getStallBlackList(), null);
                    break;

                case R.id.btn_stallAllowOpenTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.STALL_ALLOW_OPEN_TIME);
                    break;

                case R.id.btn_stallSelfOpenTime:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.STALL_SELF_OPEN_TIME);
                    break;

                case R.id.btn_stallInviteShopList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getStallInviteShopList(), null);
                    break;*/
            }
        }
    }

}
