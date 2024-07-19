package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.*;
import tkaxv7s.xposed.sesame.data.modelFieldExt.common.SelectModelFieldFunc;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.ui.dto.ModelDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelFieldInfoDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelFieldShowDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelGroupDto;
import tkaxv7s.xposed.sesame.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NewSettingsActivity extends BaseActivity {

    private static final Integer EXPORT_REQUEST_CODE = 1;

    private static final Integer IMPORT_REQUEST_CODE = 2;
    private WebView webView;
    private Context context;
    private String userId = null;
    private String userName = null;
    private Boolean debug = false;

    private final List<ModelDto> tabList = new ArrayList<>();

    private final List<ModelGroupDto> groupList = new ArrayList<>();

    @Override
    public String getBaseSubtitle() {
        return getString(R.string.settings);
    }

    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = null;
        userName = null;
        //TODO 移除
        debug = true;
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userName = intent.getStringExtra("userName");
            debug = intent.getBooleanExtra("debug", debug);
        }
        Model.initAllModel();
        UserIdMap.setCurrentUserId(userId);
        UserIdMap.load(userId);
        CooperationIdMap.load(userId);
        ReserveIdMap.load();
        BeachIdMap.load();
        ConfigV2.load(userId);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_new_settings);
        if (userName != null) {
            setBaseSubtitle(getString(R.string.settings) + ": " + userName);
        }
        setBaseSubtitleTextColor(getResources().getColor(R.color.textColorPrimary));

        context = this;

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        //settings.setPluginsEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDefaultTextEncodingName(StandardCharsets.UTF_8.name());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // 强制在当前 WebView 中加载 url
                Uri requestUrl = request.getUrl();
                String scheme = requestUrl.getScheme();
                if (
                        scheme.equalsIgnoreCase("http")
                                || scheme.equalsIgnoreCase("https")
                                || scheme.equalsIgnoreCase("ws")
                                || scheme.equalsIgnoreCase("wss")
                ) {
                    view.loadUrl(requestUrl.toString());
                    return true;
                }
                view.stopLoading();
                Toast.makeText(context, "Forbidden Scheme:\"" + scheme + "\"", Toast.LENGTH_SHORT).show();
                return false;
            }

        });
        if (debug) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.addJavascriptInterface(new WebViewCallback(), "HOOK");
        webView.loadUrl("file:///android_asset/web/index.html");
        // webView.loadUrl("http://192.168.31.32:5500/app/src/main/assets/web/index.html");
        webView.requestFocus();

        Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
        for (Map.Entry<String, ModelConfig> configEntry : modelConfigMap.entrySet()) {
            ModelConfig modelConfig = configEntry.getValue();
            tabList.add(new ModelDto(configEntry.getKey(), modelConfig.getName(), modelConfig.getIcon(), null));
        }

        for (ModelGroup modelGroup : ModelGroup.values()) {
            groupList.add(new ModelGroupDto(modelGroup.getCode(), modelGroup.getName(), modelGroup.getIcon()));
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
            save();
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onBackPressed() {
            runOnUiThread(() -> {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    NewSettingsActivity.this.finish();
                }
            });
        }

        @JavascriptInterface
        public void onExit() {
            runOnUiThread(NewSettingsActivity.this::finish);
        }
    }

    private class WebViewCallback {

        @JavascriptInterface
        public String getTabs() {
            return JsonUtil.toJsonString(tabList);
        }

        /*@JavascriptInterface
        public String getAllConfig() {
            return JsonUtil.toJsonString(ModelTask.getModelConfigMap());
        }*/

        @JavascriptInterface
        public String getVersion() {
            return "tkaxv7s.xposed.sesame v1.3.0";
        }

        @JavascriptInterface
        public String getGroup() {
            return JsonUtil.toJsonString(groupList);
        }

        @JavascriptInterface
        public String getModelByGroup(String groupCode) {
            Collection<ModelConfig> modelConfigCollection = ModelTask.getGroupModelConfig(ModelGroup.getByCode(groupCode)).values();
            List<ModelDto> modelDtoList = new ArrayList<>();
            for (ModelConfig modelConfig : modelConfigCollection) {
                List<ModelFieldShowDto> modelFields = new ArrayList<>();
                for (ModelField<?> modelField : modelConfig.getFields().values()) {
                    modelFields.add(ModelFieldShowDto.toShowDto(modelField));
                }
                modelDtoList.add(new ModelDto(modelConfig.getCode(), modelConfig.getName(), groupCode, modelFields));
            }
            return JsonUtil.toJsonString(modelDtoList);
        }

        @JavascriptInterface
        public String setModelByGroup(String groupCode, String modelsValue) {
            List<ModelDto> modelDtoList = JsonUtil.parseObject(modelsValue, new TypeReference<List<ModelDto>>() {
            });
            Map<String, ModelConfig> modelConfigSet = ModelTask.getGroupModelConfig(ModelGroup.getByCode(groupCode));
            for (ModelDto modelDto : modelDtoList) {
                ModelConfig modelConfig = modelConfigSet.get(modelDto.getModelCode());
                if (modelConfig != null) {
                    List<ModelFieldShowDto> modelFields = modelDto.getModelFields();
                    if (modelFields != null) {
                        for (ModelFieldShowDto newModelField : modelFields) {
                            if (newModelField != null) {
                                ModelField<?> modelField = modelConfig.getModelField(newModelField.getCode());
                                if (modelField != null) {
                                    modelField.setConfigValue(newModelField.getConfigValue());
                                }
                            }
                        }
                    }
                }
            }
            return "SUCCESS";
        }

        @JavascriptInterface
        public String getModel(String modelCode) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                ModelFields modelFields = modelConfig.getFields();
                List<ModelFieldShowDto> list = new ArrayList<>();
                for (ModelField<?> modelField : modelFields.values()) {
                    list.add(ModelFieldShowDto.toShowDto(modelField));
                }
                return JsonUtil.toJsonString(list);
            }
            return null;
        }

        @JavascriptInterface
        public String setModel(String modelCode, String fieldsValue) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                try {
                    ModelFields modelFields = modelConfig.getFields();
                    Map<String, ModelFieldShowDto> map = JsonUtil.parseObject(fieldsValue, new TypeReference<Map<String, ModelFieldShowDto>>() {
                    });
                    for (Map.Entry<String, ModelFieldShowDto> entry : map.entrySet()) {
                        ModelFieldShowDto newModelField = entry.getValue();
                        if (newModelField != null) {
                            ModelField<?> modelField = modelFields.get(entry.getKey());
                            if (modelField != null) {
                                modelField.setConfigValue(newModelField.getConfigValue());
                            }
                        }
                    }
                    return "SUCCESS";
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
            }
            return "FAILED";
        }

        @JavascriptInterface
        public String getField(String modelCode, String fieldCode) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                ModelField<?> modelField = modelConfig.getModelField(fieldCode);
                if (modelField != null) {
                    return JsonUtil.toJsonString(ModelFieldInfoDto.toInfoDto(modelField));
                }
            }
            return null;
        }

        @JavascriptInterface
        public String setField(String modelCode, String fieldCode, String fieldValue) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                try {
                    ModelField<?> modelField = modelConfig.getModelField(fieldCode);
                    if (modelField != null) {
                        modelField.setConfigValue(fieldValue);
                        return "SUCCESS";
                    }
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
            }
            return "FAILED";
        }

        @JavascriptInterface
        public void Log(String log) {
            Log.record("设置："+ log);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, "导出配置");
        menu.add(0, 2, 2, "导入配置");
        menu.add(0, 3, 3, "删除配置");
        menu.add(0, 4, 4, "单向好友");
        menu.add(0, 5, 5, "切换至旧UI");
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
                break;
            case 2:
                Intent importIntent = new Intent(Intent.ACTION_GET_CONTENT);
                importIntent.addCategory(Intent.CATEGORY_OPENABLE);
                importIntent.setType("*/*");
                importIntent.putExtra(Intent.EXTRA_TITLE, "config_v2.json");
                startActivityForResult(importIntent, IMPORT_REQUEST_CODE);
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
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                        .create()
                        .show();
                break;
            case 4:
                ListDialog.show(this, "单向好友列表", AlipayUser.getList(user -> user.getFriendStatus() != 1), SelectModelFieldFunc.newMapInstance(), false, ListDialog.ListType.SHOW);
                break;
            case 5:
                UIConfig.INSTANCE.setNewUI(false);
                if (UIConfig.save()) {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userName", userName);
                    finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "切换失败", Toast.LENGTH_SHORT).show();
                }
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

    private void save() {
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

}
