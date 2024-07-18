package tkaxv7s.xposed.sesame.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.Data;
import tkaxv7s.xposed.sesame.util.FileUtil;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;

import java.io.File;

@Data
public class UIConfig {

    private static final String TAG = UIConfig.class.getSimpleName();

    public static final UIConfig INSTANCE = new UIConfig();

    @JsonIgnore
    private boolean init;

    private Boolean newUI = true;

    public static Boolean save() {
        Log.record("保存UI配置");
        return FileUtil.setUIConfigFile(toSaveStr());
    }

    public static synchronized UIConfig load() {
        File uiConfigFile = FileUtil.getUIConfigFile();
        try {
            if (uiConfigFile.exists()) {
                Log.i("加载UI配置");
                String json = FileUtil.readFromFile(uiConfigFile);
                JsonUtil.copyMapper().readerForUpdating(INSTANCE).readValue(json);
                String formatted = toSaveStr();
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "格式化UI配置");
                    Log.system(TAG, "格式化UI配置");
                    FileUtil.write2File(formatted, uiConfigFile);
                }
            } else {
                unload();
                Log.i(TAG, "初始UI配置");
                Log.system(TAG, "初始UI配置");
                FileUtil.write2File(toSaveStr(), uiConfigFile);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "重置UI配置");
            Log.system(TAG, "重置UI配置");
            try {
                unload();
                FileUtil.write2File(toSaveStr(), uiConfigFile);
            } catch (Exception e) {
                Log.printStackTrace(TAG, t);
            }
        }
        INSTANCE.setInit(true);
        return INSTANCE;
    }

    public static synchronized void unload() {
        try {
            JsonUtil.copyMapper().updateValue(INSTANCE, new UIConfig());
        } catch (JsonMappingException e) {
            Log.printStackTrace(TAG, e);
        }
    }

    public static String toSaveStr() {
        return JsonUtil.toFormatJsonString(INSTANCE);
    }

}
