package tkaxv7s.xposed.sesame.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tkaxv7s.xposed.sesame.entity.UserEntity;
import tkaxv7s.xposed.sesame.util.*;

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

    public void reset() {
        for (ModelFields modelFields : modelFieldsMap.values()) {
            for (ModelField modelField : modelFields.values()) {
                if (modelField != null) {
                    modelField.reset();
                }
            }
        }
    }

    public static Boolean isModify(String userId) {
        String json = null;
        File configV2File;
        if (StringUtil.isEmpty(userId)) {
            configV2File = FileUtil.getDefaultConfigV2File();
        } else {
            configV2File = FileUtil.getConfigV2File(userId);
        }
        if (configV2File.exists()) {
            json = FileUtil.readFromFile(configV2File);
        }
        if (json != null) {
            String formatted = JsonUtil.toJsonString(INSTANCE);
            return formatted == null || !formatted.equals(json);
        }
        return true;
    }

    public static Boolean save(String userId, Boolean force) {
        if (!force) {
            if (!isModify(userId)) {
                return true;
            }
        }
        String json = JsonUtil.toJsonString(INSTANCE);

        boolean success;
        if (StringUtil.isEmpty(userId)) {
            userId = "默认";
            success = FileUtil.setDefaultConfigV2File(json);
        } else {
            success = FileUtil.setConfigV2File(userId, json);
        }
        Log.record("保存配置: " + userId);
        return success;
    }

    public static synchronized ConfigV2 load(String userId) {
        Log.i(TAG, "开始加载配置");
        String userName = "";
        File configV2File = null;
        try {
            if (StringUtil.isEmpty(userId)) {
                configV2File = FileUtil.getDefaultConfigV2File();
                userName = "默认";
            } else {
                configV2File = FileUtil.getConfigV2File(userId);
                UserEntity userEntity = UserIdMap.get(userId);
                if (userEntity == null) {
                    userName = userId;
                } else {
                    userName = userEntity.getShowName();
                }
            }
            Log.record("加载配置: "+ userName);
            if (configV2File.exists()) {
                String json = FileUtil.readFromFile(configV2File);
                JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
                String formatted = JsonUtil.toJsonString(INSTANCE);
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "格式化配置: " + userName);
                    Log.system(TAG, "格式化配置: " + userName);
                    FileUtil.write2File(formatted, configV2File);
                }
            } else {
                File defaultConfigV2File = FileUtil.getDefaultConfigV2File();
                if (defaultConfigV2File.exists()) {
                    String json = FileUtil.readFromFile(defaultConfigV2File);
                    JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
                    Log.i(TAG, "复制新配置: " + userName);
                    Log.system(TAG, "复制新配置: " + userName);
                    FileUtil.write2File(json, configV2File);
                } else {
                    INSTANCE.reset();
                    Log.i(TAG, "初始新配置: " + userName);
                    Log.system(TAG, "初始新配置: " + userName);
                    FileUtil.write2File(JsonUtil.toJsonString(INSTANCE), configV2File);
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "重置配置: " + userName);
            Log.system(TAG, "重置配置: " + userName);
            try {
                INSTANCE.reset();
                if (configV2File != null) {
                    FileUtil.write2File(JsonUtil.toJsonString(INSTANCE), configV2File);
                }
            } catch (Exception e) {
                Log.printStackTrace(TAG, t);
            }
        }
        INSTANCE.setInit(true);
        Log.i(TAG, "加载配置成功");
        return INSTANCE;
    }

}
