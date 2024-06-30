package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.*;
import tkaxv7s.xposed.sesame.util.*;

import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private Boolean isDraw = false;
    private Context context;
    private TabHost tabHost;
    private ScrollView svTabs;
    private String userId;
    //private GestureDetector gestureDetector;

    @Override
    public String getBaseSubtitle() {
        return getString(R.string.settings);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = null;
        String userName = null;
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userName = intent.getStringExtra("userName");
        }
        UserIdMap.setCurrentUid(userId, false);
        UserIdMap.load(userId);
        CooperationIdMap.load(userId);
        ReserveIdMap.load(userId);
        BeachIdMap.load(userId);
        ConfigV2.load(false);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        if (userName != null) {
            setBaseSubtitle(getString(R.string.settings) + ": " + userName);
        }
        setBaseSubtitleTextColor(getResources().getColor(R.color.textColorPrimary));

        context = this;
        tabHost = findViewById(R.id.tab_settings);
        svTabs = findViewById(R.id.sv_tabs);
        tabHost.setup();

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

        /*int size = modelConfigMap.size() - 1;
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getX() - e2.getX()) > 250) {
                    return false;
                    }
                int currentView = tabHost.getCurrentTab();
                if (e1.getY() - e2.getY() > 120 && Math.abs(velocityY) > 200) {
                    if (currentView < size) {
                        currentView++;
                    }
                    tabHost.setCurrentTab(currentView);
                } else if (e2.getY() - e1.getY() > 120 && Math.abs(velocityY) > 200) {
                    if (currentView > 0) {
                        currentView--;
                    }
                    tabHost.setCurrentTab(currentView);
                }
                return true;
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ConfigV2.isModify() && ConfigV2.save(false)) {
            Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
            try {
                sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.restart"));
            } catch (Throwable th) {
                Log.printStackTrace(th);
            }
        }
        UserIdMap.save(userId);
        CooperationIdMap.save(userId);
        ReserveIdMap.save(userId);
        BeachIdMap.save(userId);
        finish();
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }*/

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

}
