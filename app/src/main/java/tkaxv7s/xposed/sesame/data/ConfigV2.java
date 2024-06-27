package tkaxv7s.xposed.sesame.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.Data;
import tkaxv7s.xposed.sesame.util.FileUtil;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.UserIdMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ConfigV2 {

    private static final String TAG = ConfigV2.class.getSimpleName();

    public static final ConfigV2 INSTANCE = new ConfigV2();

    @JsonIgnore
    private boolean init;

    private final Map<String, ModelFields> modelFieldsMap = new ConcurrentHashMap<>();

    public void setModelFieldsMap(Map<String, ModelFields> newModels) {
        modelFieldsMap.clear();
        Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
        if (newModels == null) {
            newModels = new HashMap<>();
        }
        for (ModelConfig modelConfig : modelConfigMap.values()) {
            String modelCode = modelConfig.getCode();
            ModelFields newModelFields = new ModelFields();
            ModelFields configModelFields = modelConfig.getFields();
            ModelFields modelFields = newModels.get(modelCode);
            if (modelFields != null) {
                for (ModelField configModelField : configModelFields.values()) {
                    ModelField modelField = modelFields.get(configModelField.getCode());
                    try {
                        if (modelField != null) {
                            Object value = modelField.getValue();
                            if (value != null) {
                                configModelField.setValue(value);
                            }
                        }
                    } catch (Exception e) {
                        Log.printStackTrace(e);
                    }
                    newModelFields.addField(configModelField);
                }
            } else {
                for (ModelField configModelField : configModelFields.values()) {
                    newModelFields.addField(configModelField);
                }
            }
            modelFieldsMap.put(modelCode, newModelFields);
        }
    }

    public Boolean hasModelFields(String modelCode) {
        return modelFieldsMap.containsKey(modelCode);
    }

    /*public ModelFields getModelFields(String modelCode) {
        return modelFieldsMap.get(modelCode);
    }*/

    /*public void removeModelFields(String modelCode) {
        modelFieldsMap.remove(modelCode);
    }*/

    /*public void addModelFields(String modelCode, ModelFields modelFields) {
        modelFieldsMap.put(modelCode, modelFields);
    }*/

    public Boolean hasModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = modelFieldsMap.get(modelCode);
        if (modelFields == null) {
            return false;
        }
        return modelFields.containsKey(fieldCode);
    }

    /*public ModelField getModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = modelFieldsMap.get(modelCode);
        if (modelFields == null) {
            return null;
        }
        return modelFields.get(fieldCode);
    }*/

    /*public void removeModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return;
        }
        modelFields.remove(fieldCode);
    }*/

    /*public Boolean addModelField(String modelCode, ModelField modelField) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return false;
        }
        modelFields.put(modelCode, modelField);
        return true;
    }*/

    /*@SuppressWarnings("unchecked")
    public <T extends ModelField> T getModelFieldExt(String modelCode, String fieldCode) {
        return (T) getModelField(modelCode, fieldCode);
    }*/

    public static Boolean isModify() {
        String json = null;
        if (FileUtil.getConfigV2File(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtil.readFromFile(FileUtil.getConfigV2File(UserIdMap.getCurrentUid()));
        }
        if (json != null) {
            String formatted = JsonUtil.toJsonString(INSTANCE);
            return formatted == null || !formatted.equals(json);
        }
        return true;
    }

    public static Boolean save(Boolean force) {
        if (!force) {
            if (!isModify()) {
                return true;
            }
        }
        String json = JsonUtil.toJsonString(INSTANCE);
        Log.system(TAG, "保存 config_v2.json: " + json);
        return FileUtil.write2File(json, FileUtil.getConfigV2File());
    }

    public static synchronized ConfigV2 load() {
        Log.i(TAG, "开始加载配置");
        Model.initAllModel();
        try {
            File configV2File = FileUtil.getConfigV2File(UserIdMap.getCurrentUid());
            if (configV2File.exists()) {
                String json = FileUtil.readFromFile(configV2File);
                JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
                String formatted = JsonUtil.toJsonString(INSTANCE);
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "重新格式化 config_v2.json");
                    Log.system(TAG, "重新格式化 config_v2.json");
                    FileUtil.write2File(formatted, FileUtil.getConfigV2File(UserIdMap.getCurrentUid()));
                }
            } else {
                String formatted = JsonUtil.toJsonString(INSTANCE);
                Log.i(TAG, "初始化 config_v2.json");
                Log.system(TAG, "初始化 config_v2.json");
                FileUtil.write2File(formatted, FileUtil.getConfigV2File(UserIdMap.getCurrentUid()));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "配置文件格式有误，已重置配置文件");
            Log.system(TAG, "配置文件格式有误，已重置配置文件");
            try {
                JsonUtil.MAPPER.updateValue(INSTANCE, new ConfigV2());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, t);
            }
        }
        INSTANCE.setInit(true);
        Log.i(TAG, "加载配置成功");
        return INSTANCE;
    }

}
