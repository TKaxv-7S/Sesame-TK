package pansong291.xposed.quickenergy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.Toast;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.entity.AlipayBeach;
import pansong291.xposed.quickenergy.entity.AlipayReserve;
import pansong291.xposed.quickenergy.entity.AlipayUser;
import pansong291.xposed.quickenergy.entity.AreaCode;
import pansong291.xposed.quickenergy.entity.CooperateUser;
import pansong291.xposed.quickenergy.util.BeachIdMap;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.CooperationIdMap;
import pansong291.xposed.quickenergy.util.FriendIdMap;
import pansong291.xposed.quickenergy.util.LanguageUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.ReserveIdMap;

public class SettingsActivity extends Activity {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int MAX_TAB_INDEX = 3;

    private TabHost tabHost;
    private GestureDetector gestureDetector;
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;

    Switch sw_immediateEffect, sw_recordLog, sw_showToast, sw_stayAwake, sw_timeoutRestart, sw_startAt7,
            sw_collectWateringBubble, sw_collectProp, sw_collectEnergy, sw_helpFriendCollect, sw_receiveForestTaskAward,
            sw_cooperateWater, sw_energyRain, sw_enableFarm, sw_rewardFriend, sw_sendBackAnimal,
            sw_receiveFarmToolReward, sw_useNewEggTool, sw_harvestProduce, sw_donation, sw_answerQuestion,
            sw_receiveFarmTaskAward, sw_feedAnimal, sw_useAccelerateTool, sw_notifyFriend, sw_receivePoint,
            sw_openTreasureBox, sw_donateCharityCoin, sw_kbSignIn, sw_limitCollect, sw_doubleCard,
            sw_ExchangeEnergyDoubleClick, sw_reserve, sw_ecoLifeTick, sw_tiyubiz, sw_insBlueBeanExchange,
            sw_ancientTree, sw_ancientTreeOnlyWeek, sw_receiveCoinAsset, sw_antdodoCollect, sw_recordFarmGame, sw_beach,
            sw_kitchen, sw_antOcean, sw_userPatrol, sw_animalConsumeProp, sw_antOrchard, sw_receiveOrchardTaskAward,
            sw_enableOnGoing, sw_collectSesame, sw_zcjSignIn, sw_merchantKmdk, sw_acceptGift,
            sw_enableStall, sw_stallAutoClose, sw_stallAutoOpen, sw_stallAutoTask, sw_stallReceiveAward,
            sw_stallOpenType, sw_stallDonate, sw_chickenDiary, sw_collectGiftBox, sw_stallInviteRegister,
            sw_stallThrowManure, sw_greenFinance, sw_totalCertCount, sw_batchRobEnergy, sw_antBookRead, sw_consumeGold,
            sw_omegakoiTown, sw_newRpc, sw_debugMode, sw_language_simplified_chinese, sw_special_food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings);

        initTabHost();

        initFlipper();

        Config.load();
        FriendIdMap.shouldReload = true;
        CooperationIdMap.shouldReload = true;
        ReserveIdMap.shouldReload = true;
        BeachIdMap.shouldReload = true;

        initSwitch();

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
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

    private void initTabHost() {
        tabHost = findViewById(R.id.tab_settings);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("base")
                .setIndicator(getString(R.string.base_configuration))
                .setContent(R.id.tab_base);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("forest")
                .setIndicator(getString(R.string.forest_configuration))
                .setContent(R.id.tab_forest);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("farm")
                .setIndicator(getString(R.string.farm_configuration))
                .setContent(R.id.tab_farm);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("other")
                .setIndicator(getString(R.string.other_configuration))
                .setContent(R.id.tab_other);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
    }

    private void initSwitch() {
        sw_immediateEffect = findViewById(R.id.sw_immediateEffect);
        sw_recordLog = findViewById(R.id.sw_recordLog);
        sw_showToast = findViewById(R.id.sw_showToast);
        sw_stayAwake = findViewById(R.id.sw_stayAwake);
        sw_timeoutRestart = findViewById(R.id.sw_timeoutRestart);
        sw_startAt7 = findViewById(R.id.sw_startAt7);
        sw_enableOnGoing = findViewById(R.id.sw_enableOnGoing);
        sw_newRpc = findViewById(R.id.sw_newRpc);
        sw_debugMode = findViewById(R.id.sw_debugMode);
        sw_language_simplified_chinese = findViewById(R.id.sw_languageSimplifiedChinese);

        sw_collectEnergy = findViewById(R.id.sw_collectEnergy);
        sw_collectWateringBubble = findViewById(R.id.sw_collectWateringBubble);
        sw_batchRobEnergy = findViewById(R.id.sw_batchRobEnergy);
        sw_collectProp = findViewById(R.id.sw_collectProp);
        sw_helpFriendCollect = findViewById(R.id.sw_helpFriendCollect);
        sw_receiveForestTaskAward = findViewById(R.id.sw_receiveForestTaskAward);
        sw_cooperateWater = findViewById(R.id.sw_cooperateWater);
        sw_ancientTree = findViewById(R.id.sw_ancientTree);
        sw_energyRain = findViewById(R.id.sw_energyRain);
        sw_reserve = findViewById(R.id.sw_reserve);
        sw_beach = findViewById(R.id.sw_beach);
        sw_enableFarm = findViewById(R.id.sw_enableFarm);
        sw_rewardFriend = findViewById(R.id.sw_rewardFriend);
        sw_sendBackAnimal = findViewById(R.id.sw_sendBackAnimal);
        sw_receiveFarmToolReward = findViewById(R.id.sw_receiveFarmToolReward);
        sw_recordFarmGame = findViewById(R.id.sw_recordFarmGame);
        sw_kitchen = findViewById(R.id.sw_kitchen);
        sw_special_food = findViewById(R.id.sw_special_food);
        sw_useNewEggTool = findViewById(R.id.sw_useNewEggTool);
        sw_harvestProduce = findViewById(R.id.sw_harvestProduce);
        sw_donation = findViewById(R.id.sw_donation);
        sw_answerQuestion = findViewById(R.id.sw_answerQuestion);
        sw_receiveFarmTaskAward = findViewById(R.id.sw_receiveFarmTaskAward);
        sw_feedAnimal = findViewById(R.id.sw_feedAnimal);
        sw_useAccelerateTool = findViewById(R.id.sw_useAccelerateTool);
        sw_notifyFriend = findViewById(R.id.sw_notifyFriend);
        sw_acceptGift = findViewById(R.id.sw_acceptGift);
        sw_chickenDiary = findViewById(R.id.sw_chickenDiary);
        sw_antOrchard = findViewById(R.id.sw_antOrchard);
        sw_receiveOrchardTaskAward = findViewById(R.id.sw_receiveOrchardTaskAward);
        sw_receivePoint = findViewById(R.id.sw_receivePoint);
        sw_openTreasureBox = findViewById(R.id.sw_openTreasureBox);
        sw_receiveCoinAsset = findViewById(R.id.sw_receiveCoinAsset);
        sw_donateCharityCoin = findViewById(R.id.sw_donateCharityCoin);
        sw_kbSignIn = findViewById(R.id.sw_kbSignIn);
        sw_limitCollect = findViewById(R.id.sw_limitCollect);
        sw_doubleCard = findViewById(R.id.sw_doubleCard);
        sw_ExchangeEnergyDoubleClick = findViewById(R.id.sw_ExchangeEnergyDoubleClick);
        sw_ecoLifeTick = findViewById(R.id.sw_ecoLifeTick);
        sw_tiyubiz = findViewById(R.id.sw_tiyubiz);
        sw_insBlueBeanExchange = findViewById(R.id.sw_insBlueBeanExchange);
        sw_collectSesame = findViewById(R.id.sw_collectSesame);
        sw_zcjSignIn = findViewById(R.id.sw_zcjSignIn);
        sw_merchantKmdk = findViewById(R.id.sw_merchantKmdk);
        sw_ancientTreeOnlyWeek = findViewById(R.id.sw_ancientTreeOnlyWeek);
        sw_antdodoCollect = findViewById(R.id.sw_antdodoCollect);
        sw_antOcean = findViewById(R.id.sw_antOcean);
        sw_userPatrol = findViewById(R.id.sw_userPatrol);
        sw_animalConsumeProp = findViewById(R.id.sw_animalConsumeProp);
        sw_collectGiftBox = findViewById(R.id.sw_collectGiftBox);
        sw_totalCertCount = findViewById(R.id.sw_totalCertCount);

        sw_enableStall = findViewById(R.id.sw_enableStall);
        sw_stallAutoClose = findViewById(R.id.sw_stallAutoClose);
        sw_stallAutoOpen = findViewById(R.id.sw_stallAutoOpen);
        sw_stallAutoTask = findViewById(R.id.sw_stallAutoTask);
        sw_stallReceiveAward = findViewById(R.id.sw_stallReceiveAward);
        sw_stallOpenType = findViewById(R.id.sw_stallOpenType);
        sw_stallDonate = findViewById(R.id.sw_stallDonate);
        sw_stallInviteRegister = findViewById(R.id.sw_stallInviteRegister);
        sw_stallThrowManure = findViewById(R.id.sw_stallThrowManure);
        sw_greenFinance = findViewById(R.id.sw_greenFinance);
        //sw_antBookRead = findViewById(R.id.sw_antBookRead);
        //sw_consumeGold = findViewById(R.id.sw_consumeGold);
        //sw_omegakoiTown = findViewById(R.id.sw_omegakoiTown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config config = Config.INSTANCE;
        sw_immediateEffect.setChecked(config.isImmediateEffect());
        sw_recordLog.setChecked(config.isRecordLog());
        sw_showToast.setChecked(config.isShowToast());
        sw_stayAwake.setChecked(config.isStayAwake());
        sw_timeoutRestart.setChecked(config.isTimeoutRestart());
        sw_startAt7.setChecked(config.isStartAt7());
        sw_enableOnGoing.setChecked(config.isEnableOnGoing());
        sw_newRpc.setChecked(config.isNewRpc());
        sw_debugMode.setChecked(config.isDebugMode());
        sw_language_simplified_chinese.setChecked(config.isLanguageSimplifiedChinese());

        sw_collectEnergy.setChecked(config.isCollectEnergy());
        sw_collectWateringBubble.setChecked(config.isCollectWateringBubble());
        sw_batchRobEnergy.setChecked(config.isBatchRobEnergy());
        sw_collectProp.setChecked(config.isCollectProp());
        sw_helpFriendCollect.setChecked(config.isHelpFriendCollect());
        sw_receiveForestTaskAward.setChecked(config.isReceiveForestTaskAward());
        sw_cooperateWater.setChecked(config.isCooperateWater());
        sw_ancientTree.setChecked(config.isAncientTree());
        sw_energyRain.setChecked(config.isEnergyRain());
        sw_reserve.setChecked(config.isReserve());
        sw_beach.setChecked(config.isBeach());
        sw_enableFarm.setChecked(config.isEnableFarm());
        sw_rewardFriend.setChecked(config.isRewardFriend());
        sw_sendBackAnimal.setChecked(config.isSendBackAnimal());
        sw_receiveFarmToolReward.setChecked(config.isReceiveFarmToolReward());
        sw_recordFarmGame.setChecked(config.isRecordFarmGame());
        sw_kitchen.setChecked(config.isKitchen());
        sw_special_food.setChecked(config.isUseSpecialFood());
        sw_useNewEggTool.setChecked(config.isUseNewEggTool());
        sw_harvestProduce.setChecked(config.isHarvestProduce());
        sw_donation.setChecked(config.isDonation());
        sw_answerQuestion.setChecked(config.isAnswerQuestion());
        sw_receiveFarmTaskAward.setChecked(config.isReceiveFarmTaskAward());
        sw_feedAnimal.setChecked(config.isFeedAnimal());
        sw_useAccelerateTool.setChecked(config.isUseAccelerateTool());
        sw_notifyFriend.setChecked(config.isNotifyFriend());
        sw_acceptGift.setChecked(config.isAcceptGift());
        sw_chickenDiary.setChecked(config.isChickenDiary());
        sw_antOrchard.setChecked(config.isAntOrchard());
        sw_receiveOrchardTaskAward.setChecked(config.isReceiveOrchardTaskAward());
        sw_receivePoint.setChecked(config.isReceivePoint());
        sw_openTreasureBox.setChecked(config.isOpenTreasureBox());
        sw_receiveCoinAsset.setChecked(config.isReceiveCoinAsset());
        sw_donateCharityCoin.setChecked(config.isDonateCharityCoin());
        sw_kbSignIn.setChecked(config.isKbSignIn());
        sw_limitCollect.setChecked(config.isLimitCollect());
        sw_doubleCard.setChecked(config.isDoubleCard());
        sw_ExchangeEnergyDoubleClick.setChecked(config.isExchangeEnergyDoubleClick());
        sw_ecoLifeTick.setChecked(config.isEcoLifeTick());
        sw_tiyubiz.setChecked(config.isTiyubiz());
        sw_insBlueBeanExchange.setChecked(config.isInsBlueBeanExchange());
        sw_collectSesame.setChecked(config.isCollectSesame());
        sw_zcjSignIn.setChecked(config.isZcjSignIn());
        sw_merchantKmdk.setChecked(config.isMerchantKmdk());
        sw_ancientTreeOnlyWeek.setChecked(config.isAncientTreeOnlyWeek());
        sw_antdodoCollect.setChecked(config.isAntdodoCollect());
        sw_antOcean.setChecked(config.isAntOcean());
        sw_userPatrol.setChecked(config.isUserPatrol());
        sw_animalConsumeProp.setChecked(config.isAnimalConsumeProp());
        sw_collectGiftBox.setChecked(config.isCollectGiftBox());
        sw_totalCertCount.setChecked(config.isTotalCertCount());

        sw_enableStall.setChecked(config.isEnableStall());
        sw_stallAutoClose.setChecked(config.isStallAutoClose());
        sw_stallAutoOpen.setChecked(config.isStallAutoOpen());
        sw_stallAutoTask.setChecked(config.isStallAutoTask());
        sw_stallReceiveAward.setChecked(config.isStallReceiveAward());
        sw_stallOpenType.setChecked(config.isStallOpenType());
        sw_stallDonate.setChecked(config.isStallDonate());
        sw_stallInviteRegister.setChecked(config.isStallInviteRegister());
        sw_stallThrowManure.setChecked(config.isStallThrowManure());
        sw_greenFinance.setChecked(config.isGreenFinance());
        //sw_antBookRead.setChecked(config.isAntBookRead());
        //sw_consumeGold.setChecked(config.isConsumeGold());
        //sw_omegakoiTown.setChecked(config.isOmegakoiTown());
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        Config config = Config.INSTANCE;
        if (v instanceof Switch) {
            Switch sw = (Switch) v;
            switch (v.getId()) {
                case R.id.sw_immediateEffect:
                    config.setImmediateEffect(sw.isChecked());
                    break;

                case R.id.sw_recordLog:
                    config.setRecordLog(sw.isChecked());
                    break;

                case R.id.sw_showToast:
                    config.setShowToast(sw.isChecked());
                    break;

                case R.id.sw_stayAwake:
                    config.setStayAwake(sw.isChecked());
                    break;

                case R.id.sw_timeoutRestart:
                    config.setTimeoutRestart(sw.isChecked());
                    break;

                case R.id.sw_startAt7:
                    config.setStartAt7(sw.isChecked());
                    break;

                case R.id.sw_enableOnGoing:
                    config.setEnableOnGoing(sw.isChecked());
                    break;

                case R.id.sw_newRpc:
                    config.setNewRpc(sw.isChecked());
                    break;

                case R.id.sw_debugMode:
                    config.setDebugMode(sw.isChecked());
                    break;

                case R.id.sw_languageSimplifiedChinese:
                    config.setLanguageSimplifiedChinese(sw.isChecked());
                    // 提示需要重启 language_simplified_chinese_need_restart
                    Toast.makeText(this, R.string.language_simplified_chinese_need_restart, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.sw_collectEnergy:
                    config.setCollectEnergy(sw.isChecked());
                    break;

                case R.id.sw_collectWateringBubble:
                    config.setCollectWateringBubble(sw.isChecked());
                    break;

                case R.id.sw_batchRobEnergy:
                    config.setBatchRobEnergy(sw.isChecked());
                    break;

                case R.id.sw_collectProp:
                    config.setCollectProp(sw.isChecked());
                    break;

                case R.id.sw_limitCollect:
                    config.setLimitCollect(sw.isChecked());
                    break;

                case R.id.sw_doubleCard:
                    config.setDoubleCard(sw.isChecked());
                    break;

                case R.id.sw_helpFriendCollect:
                    config.setHelpFriendCollect(sw.isChecked());
                    break;

                case R.id.sw_receiveForestTaskAward:
                    config.setReceiveForestTaskAward(sw.isChecked());
                    break;

                case R.id.sw_cooperateWater:
                    config.setCooperateWater(sw.isChecked());
                    break;

                case R.id.sw_ancientTree:
                    config.setAncientTree(sw.isChecked());
                    break;

                case R.id.sw_energyRain:
                    config.setEnergyRain(sw.isChecked());
                    break;

                case R.id.sw_ExchangeEnergyDoubleClick:
                    config.setExchangeEnergyDoubleClick(sw.isChecked());
                    break;

                case R.id.sw_reserve:
                    config.setReserve(sw.isChecked());
                    break;

                case R.id.sw_beach:
                    config.setBeach(sw.isChecked());
                    break;

                case R.id.sw_enableFarm:
                    config.setEnableFarm(sw.isChecked());
                    break;

                case R.id.sw_rewardFriend:
                    config.setRewardFriend(sw.isChecked());
                    break;

                case R.id.sw_sendBackAnimal:
                    config.setSendBackAnimal(sw.isChecked());
                    break;

                case R.id.sw_receiveFarmToolReward:
                    config.setReceiveFarmToolReward(sw.isChecked());
                    break;

                case R.id.sw_recordFarmGame:
                    config.setRecordFarmGame(sw.isChecked());
                    break;

                case R.id.sw_kitchen:
                    config.setKitchen(sw.isChecked());
                    break;

                case R.id.sw_special_food:
                    config.setUseSpecialFood(sw.isChecked());
                    break;

                case R.id.sw_useNewEggTool:
                    config.setUseNewEggTool(sw.isChecked());
                    break;

                case R.id.sw_harvestProduce:
                    config.setHarvestProduce(sw.isChecked());
                    break;

                case R.id.sw_donation:
                    config.setDonation(sw.isChecked());
                    break;

                case R.id.sw_answerQuestion:
                    config.setAnswerQuestion(sw.isChecked());
                    break;

                case R.id.sw_receiveFarmTaskAward:
                    config.setReceiveFarmTaskAward(sw.isChecked());
                    break;

                case R.id.sw_feedAnimal:
                    config.setFeedAnimal(sw.isChecked());
                    break;

                case R.id.sw_useAccelerateTool:
                    config.setUseAccelerateTool(sw.isChecked());
                    break;

                case R.id.sw_notifyFriend:
                    config.setNotifyFriend(sw.isChecked());
                    break;

                case R.id.sw_acceptGift:
                    config.setAcceptGift(sw.isChecked());
                    break;

                case R.id.sw_chickenDiary:
                    config.setChickenDiary(sw.isChecked());
                    break;

                case R.id.sw_antOrchard:
                    config.setAntOrchard(sw.isChecked());
                    break;

                case R.id.sw_receiveOrchardTaskAward:
                    config.setReceiveOrchardTaskAward(sw.isChecked());
                    break;

                case R.id.sw_receivePoint:
                    config.setReceivePoint(sw.isChecked());
                    break;

                case R.id.sw_openTreasureBox:
                    config.setOpenTreasureBox(sw.isChecked());
                    break;

                case R.id.sw_receiveCoinAsset:
                    config.setReceiveCoinAsset(sw.isChecked());
                    break;

                case R.id.sw_donateCharityCoin:
                    config.setDonateCharityCoin(sw.isChecked());
                    break;

                case R.id.sw_kbSignIn:
                    config.setKbSignIn(sw.isChecked());
                    break;

                case R.id.sw_ecoLifeTick:
                    config.setEcoLifeTick(sw.isChecked());
                    break;

                case R.id.sw_tiyubiz:
                    config.setTiyubiz(sw.isChecked());
                    break;

                case R.id.sw_insBlueBeanExchange:
                    config.setInsBlueBeanExchange(sw.isChecked());
                    break;

                case R.id.sw_collectSesame:
                    config.setCollectSesame(sw.isChecked());
                    break;

                case R.id.sw_zcjSignIn:
                    config.setZcjSignIn(sw.isChecked());
                    break;

                case R.id.sw_merchantKmdk:
                    config.setMerchantKmdk(sw.isChecked());
                    break;

                case R.id.sw_ancientTreeOnlyWeek:
                    config.setAncientTreeOnlyWeek(sw.isChecked());
                    break;

                case R.id.sw_antdodoCollect:
                    config.setAntdodoCollect(sw.isChecked());
                    break;

                case R.id.sw_antOcean:
                    config.setAntOcean(sw.isChecked());
                    break;

                case R.id.sw_userPatrol:
                    config.setUserPatrol(sw.isChecked());
                    break;

                case R.id.sw_animalConsumeProp:
                    config.setAnimalConsumeProp(sw.isChecked());
                    break;

                case R.id.sw_collectGiftBox:
                    config.setCollectGiftBox(sw.isChecked());
                    break;

                case R.id.sw_totalCertCount:
                    config.setTotalCertCount(sw.isChecked());
                    break;

                case R.id.sw_enableStall:
                    config.setEnableStall(sw.isChecked());
                    break;

                case R.id.sw_stallAutoClose:
                    config.setStallAutoClose(sw.isChecked());
                    break;

                case R.id.sw_stallAutoOpen:
                    config.setStallAutoOpen(sw.isChecked());
                    break;

                case R.id.sw_stallAutoTask:
                    config.setStallAutoTask(sw.isChecked());
                    break;

                case R.id.sw_stallReceiveAward:
                    config.setStallReceiveAward(sw.isChecked());
                    break;

                case R.id.sw_stallOpenType:
                    config.setStallOpenType(sw.isChecked());
                    break;

                case R.id.sw_stallDonate:
                    config.setStallDonate(sw.isChecked());
                    break;

                case R.id.sw_stallInviteRegister:
                    config.setStallInviteRegister(sw.isChecked());
                    break;

                case R.id.sw_stallThrowManure:
                    config.setStallThrowManure(sw.isChecked());
                    break;

                case R.id.sw_greenFinance:
                    config.setGreenFinance(sw.isChecked());
                    break;

                /*case R.id.sw_antBookRead:
                    config.setAntBookRead(sw.isChecked());
                    break;*/

                /*case R.id.sw_consumeGold:
                    config.setConsumeGold(sw.isChecked());
                    break;*/

                /*case R.id.sw_omegakoiTown:
                    config.setOmegakoiTown(sw.isChecked());
                    break;*/
            }
        } else if (v instanceof Button) {
            Button btn = (Button) v;
            switch (v.getId()) {
                case R.id.btn_toastOffsetY:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.TOAST_OFFSET_Y);
                    break;

                case R.id.btn_waitWhenException:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.WAIT_WHEN_EXCEPTION);
                    break;

                case R.id.btn_checkInterval:
                    EditDialog.showEditDialog(this, btn.getText(), EditDialog.EditMode.CHECK_INTERVAL);
                    break;

                case R.id.btn_advanceTime:
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

                case R.id.btn_sendType:
                    ChoiceDialog.showSendType(this, btn.getText());
                    break;

                case R.id.btn_dontSendFriendList:
                    ListDialog.show(this, btn.getText(), AlipayUser.getList(), config.getDontSendFriendList(), null);
                    break;

                case R.id.btn_recallAnimalType:
                    ChoiceDialog.showRecallAnimalType(this, btn.getText());
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
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Config.isModify() && Config.save(false)) {
            Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
            try {
                this.sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.restart"));
            } catch (Throwable th) {
                Log.printStackTrace(th);
            }
        }
        FriendIdMap.saveIdMap();
        CooperationIdMap.saveIdMap();
        ReserveIdMap.saveIdMap();
        BeachIdMap.saveIdMap();
    }

}
