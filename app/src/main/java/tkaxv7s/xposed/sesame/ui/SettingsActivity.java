package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import java.util.Map;

import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ConfigV2;
import tkaxv7s.xposed.sesame.data.ModelConfig;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ViewAppInfo;
import tkaxv7s.xposed.sesame.data.ModelTask;
import tkaxv7s.xposed.sesame.util.BeachIdMap;
import tkaxv7s.xposed.sesame.util.CooperationIdMap;
import tkaxv7s.xposed.sesame.util.LanguageUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.ReserveIdMap;
import tkaxv7s.xposed.sesame.util.UserIdMap;

public class SettingsActivity extends Activity {

    private Boolean isDraw = false;
    private Context context;
    private TabHost tabHost;
    private ScrollView svTabs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigV2.load();
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(getText(R.string.settings) + " " + ViewAppInfo.getAppVersion());

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
                sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.restart"));
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
