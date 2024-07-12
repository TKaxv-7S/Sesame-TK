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
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.*;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.ui.dto.ModelDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelFieldInfoDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelFieldShowDto;
import tkaxv7s.xposed.sesame.ui.dto.ModelFieldsDto;
import tkaxv7s.xposed.sesame.util.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends BaseActivity {
    private WebView webView;
    private Context context;

    private Boolean isSave = true;
    private String userId = null;
    private String userName = null;
    private Boolean debug = false;

    private final List<ModelDto> tabList = new ArrayList<>();

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
        setContentView(R.layout.activity_settings);
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
        webView.requestFocus();

        Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
        for (Map.Entry<String, ModelConfig> configEntry : modelConfigMap.entrySet()) {
            ModelConfig modelConfig = configEntry.getValue();
            tabList.add(new ModelDto(configEntry.getKey(), modelConfig.getName(), modelConfig.getIcon()));
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onBackPressed() {
            runOnUiThread(() -> {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    SettingsActivity.this.finish();
                }
            });
        }

        @JavascriptInterface
        public void onExit() {
            runOnUiThread(SettingsActivity.this::finish);
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
        public String getModel(String modelCode) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                ModelFields modelFields = modelConfig.getFields();
                ModelFieldsDto modelFieldsDto = new ModelFieldsDto();
                for (ModelField<?> modelField : modelFields.values()) {
                    modelFieldsDto.addField(ModelFieldShowDto.toShowDto(modelField));
                }
                return JsonUtil.toJsonString(modelFieldsDto);
            }
            return null;
        }

        @JavascriptInterface
        public String setModel(String modelCode, String fieldsValue) {
            ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
            if (modelConfig != null) {
                try {
                    ModelFields modelFields = modelConfig.getFields();
                    ModelFieldsDto newModelFields = JsonUtil.parseObject(fieldsValue, ModelFieldsDto.class);
                    for (ModelField<?> modelField : modelFields.values()) {
                        ModelFieldShowDto newModelField = newModelFields.get(modelField.getCode());
                        if (newModelField != null) {
                            modelField.setConfigValue(newModelField.getConfigValue());
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
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSave = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, "删除配置");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
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
        }
        return super.onOptionsItemSelected(item);
    }

}
