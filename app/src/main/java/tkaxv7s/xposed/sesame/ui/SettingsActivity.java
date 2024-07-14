package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.*;
import tkaxv7s.xposed.sesame.data.modelFieldExt.common.SelectModelFieldFunc;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private static final Integer EXPORT_REQUEST_CODE = 1;

    private static final Integer IMPORT_REQUEST_CODE = 2;

    private Context context;
    private Boolean isDraw = false;
    private TabHost tabHost;
    private ScrollView svTabs;
    private String userId;
    private String userName;
    private Boolean isSave;
    private Boolean isHold;
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
        userName = null;
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userName = intent.getStringExtra("userName");
        }
        Model.initAllModel();
        UserIdMap.setCurrentUserId(userId);
        UserIdMap.load(userId);
        CooperationIdMap.load(userId);
        ReserveIdMap.load();
        BeachIdMap.load();
        ConfigV2.load(userId);
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
        if (isSave) {
            if (ConfigV2.isModify(userId) && ConfigV2.save(userId, false)) {
                Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
                if (!StringUtil.isEmpty(userId)) {
                    try {
                        Intent intent = new Intent("com.eg.android.AlipayGphone.sesame.restart");
                        intent.putExtra("userId", userId);
                        sendBroadcast(intent);
                    } catch (Throwable th) {
                        Log.printStackTrace(th);
                    }
                }
            }
            if (!StringUtil.isEmpty(userId)) {
                UserIdMap.save(userId);
                CooperationIdMap.save(userId);
            }
        }
        if (!isHold) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSave = true;
        isHold = false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, "导出配置");
        menu.add(0, 2, 2, "导入配置");
        menu.add(0, 3, 3, "删除配置");
        menu.add(0, 4, 4, "单向好友");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
                exportIntent.setType("*/*");
                exportIntent.putExtra(Intent.EXTRA_TITLE, "[" + userName + "]-config_v2.json");
                startActivityForResult(exportIntent, EXPORT_REQUEST_CODE);
                isHold = true;
                break;
            case 2:
                Intent importIntent = new Intent(Intent.ACTION_GET_CONTENT);
                importIntent.addCategory(Intent.CATEGORY_OPENABLE);
                importIntent.setType("*/*");
                importIntent.putExtra(Intent.EXTRA_TITLE, "config_v2.json");
                startActivityForResult(importIntent, IMPORT_REQUEST_CODE);
                isHold = true;
                break;
            case 3:
                new AlertDialog.Builder(context)
                        .setTitle("警告")
                        .setMessage("确认删除该配置？")
                        .setPositiveButton(R.string.ok, (dialog, id) -> {
                            File userConfigDirectoryFile;
                            if (StringUtil.isEmpty(userId)) {
                                userConfigDirectoryFile = FileUtil.getDefaultConfigV2File();
                            } else {
                                userConfigDirectoryFile = FileUtil.getUserConfigDirectoryFile(userId);
                            }
                            if (FileUtil.deleteFile(userConfigDirectoryFile)) {
                                Toast.makeText(this, "配置删除成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "配置删除失败", Toast.LENGTH_SHORT).show();
                            }
                            isSave = false;
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                        .create()
                        .show();
                break;
            case 4:
                ListDialog.show(this, "单向好友列表", AlipayUser.getList(user -> user.getFriendStatus() != 1), SelectModelFieldFunc.newMapInstance(), false, ListDialog.ListType.SHOW);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == EXPORT_REQUEST_CODE) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    File configV2File;
                    if (StringUtil.isEmpty(userId)) {
                        configV2File = FileUtil.getDefaultConfigV2File();
                    } else {
                        configV2File = FileUtil.getConfigV2File(userId);
                    }
                    FileInputStream inputStream = new FileInputStream(configV2File);
                    if (FileUtil.streamTo(inputStream, getContentResolver().openOutputStream(data.getData()))) {
                        Toast.makeText(this, "导出成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "导出失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.printStackTrace(e);
                    Toast.makeText(this, "导出失败！", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == IMPORT_REQUEST_CODE) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    File configV2File;
                    if (StringUtil.isEmpty(userId)) {
                        configV2File = FileUtil.getDefaultConfigV2File();
                    } else {
                        configV2File = FileUtil.getConfigV2File(userId);
                    }
                    FileOutputStream outputStream = new FileOutputStream(configV2File);
                    if (FileUtil.streamTo(getContentResolver().openInputStream(data.getData()), outputStream)) {
                        Toast.makeText(this, "导入成功！", Toast.LENGTH_SHORT).show();
                        if (!StringUtil.isEmpty(userId)) {
                            try {
                                Intent intent = new Intent("com.eg.android.AlipayGphone.sesame.restart");
                                intent.putExtra("userId", userId);
                                sendBroadcast(intent);
                            } catch (Throwable th) {
                                Log.printStackTrace(th);
                            }
                        }
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "导入失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.printStackTrace(e);
                    Toast.makeText(this, "导入失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
