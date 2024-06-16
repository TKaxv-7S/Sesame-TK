package pansong291.xposed.quickenergy.data;

import android.os.Build;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.Getter;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.util.FileUtils;
import pansong291.xposed.quickenergy.util.JsonUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.UserIdMap;

@Data
public class ConfigV2 {

    private static final String TAG = ConfigV2.class.getSimpleName();

    public static final ConfigV2 INSTANCE = new ConfigV2();

    @Getter
    private static volatile boolean init;

    private boolean immediateEffect = true;
    private boolean recordLog = true;
    private boolean showToast = true;
    private int toastOffsetY = 0;
    private int checkInterval = 1800_000;
    private boolean stayAwake = true;
    private boolean timeoutRestart = true;
    private boolean startAt0 = true;
    private boolean startAt7 = true;
    private boolean enableOnGoing = false;
    private boolean batteryPerm = true;
    private boolean newRpc = true;
    private boolean debugMode = false;
    private boolean languageSimplifiedChinese = false;
    private int waitWhenException = 60 * 60 * 1000;

    private final Map<String, ModelFields> modelFieldsMap = new ConcurrentHashMap<>();

    public void setModelFieldsMap(Map<String, ModelFields> newModels) {
        modelFieldsMap.clear();
        if (newModels != null) {
            Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
            for (Map.Entry<String, ModelFields> modelFieldsEntry : newModels.entrySet()) {
                ModelFields newModelFields = modelFieldsEntry.getValue();
                if (newModelFields != null) {
                    String modelCode = modelFieldsEntry.getKey();
                    ModelConfig modelConfig = modelConfigMap.get(modelCode);
                    if (modelConfig != null) {
                        ModelFields configModelFields = modelConfig.getFields();
                        for (Map.Entry<String, ModelField> modelFieldEntry : newModelFields.entrySet()) {
                            ModelField modelField = modelFieldEntry.getValue();
                            if (modelField != null) {
                                ModelField configModelField = configModelFields.get(modelFieldEntry.getKey());
                                if (configModelField != null) {
                                    try {
                                        configModelField.setValue(modelField.getValue());
                                    } catch (Exception e) {
                                        Log.printStackTrace(e);
                                    }
                                }
                            }
                        }
                        modelFieldsMap.put(modelCode, configModelFields);
                    }
                }
            }
        }
    }

    public Boolean hasModelFields(String modelCode) {
        return modelFieldsMap.containsKey(modelCode);
    }

    public ModelFields getModelFields(String modelCode) {
        return getModelFields(modelCode, false);
    }

    public ModelFields getModelFields(String modelCode, Boolean isCreate) {
        if (isCreate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return modelFieldsMap.compute(modelCode, (key, value) -> {
                    if (value == null) {
                        ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                        if (modelConfig != null) {
                            value = new ModelFields();
                            value.putAll(modelConfig.getFields());
                        }
                    }
                    return value;
                });
            } else {
                ModelFields modelFields = modelFieldsMap.get(modelCode);
                if (modelFields == null) {
                    ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                    if (modelConfig != null) {
                        modelFields = new ModelFields();
                        modelFields.putAll(modelConfig.getFields());
                        modelFieldsMap.put(modelCode, modelFields);
                    }
                }
                return modelFields;
            }
        } else {
            return modelFieldsMap.get(modelCode);
        }
    }

    /*public void removeModelFields(String modelCode) {
        modelFieldsMap.remove(modelCode);
    }*/

    /*public void addModelFields(String modelCode, ModelFields modelFields) {
        modelFieldsMap.put(modelCode, modelFields);
    }*/

    public Boolean hasModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return false;
        }
        return modelFields.containsKey(fieldCode);
    }

    public ModelField getModelField(String modelCode, String fieldCode) {
        return getModelField(modelCode, fieldCode, false);
    }

    public ModelField getModelField(String modelCode, String fieldCode, Boolean isCreate) {
        ModelFields modelFields = getModelFields(modelCode, isCreate);
        if (modelFields == null) {
            return null;
        }
        if (isCreate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return modelFields.compute(fieldCode, (key, value)-> {
                    if (value == null) {
                        ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                        if (modelConfig != null) {
                            ModelField modelField = modelConfig.getFields().get(fieldCode);
                            if (modelField != null) {
                                value = modelField.clone();
                            }
                        }
                    }
                    return value;
                });
            } else {
                ModelField modelField = modelFields.get(fieldCode);
                if (modelField == null) {
                    ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                    if (modelConfig != null) {
                        modelField = modelConfig.getFields().get(fieldCode);
                        if (modelField != null) {
                            modelField = modelField.clone();
                            modelFields.put(fieldCode, modelField);
                        }
                    }
                }
                return modelField;
            }
        } else {
            return modelFields.get(fieldCode);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelField> T getModelFieldExt(String modelCode, String fieldCode) {
        return getModelFieldExt(modelCode, fieldCode, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelField> T getModelFieldExt(String modelCode, String fieldCode, Boolean isCreate) {
        return (T) getModelField(modelCode, fieldCode, isCreate);
    }

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

    public static Boolean isModify() {
        String json = null;
        if (FileUtils.getConfigV2File(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigV2File(UserIdMap.getCurrentUid()));
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
        return FileUtils.write2File(json, FileUtils.getConfigV2File());
    }

    public static synchronized ConfigV2 load() {
        ModelTask.initAllModel();
        Log.i(TAG, "load config_v2");
        String json = null;
        if (FileUtils.getConfigV2File(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigV2File(UserIdMap.getCurrentUid()));
        }
        try {
            JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
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
        String formatted = JsonUtil.toJsonString(INSTANCE);
        if (formatted != null && !formatted.equals(json)) {
            Log.i(TAG, "重新格式化 config_v2.json");
            Log.system(TAG, "重新格式化 config_v2.json");
            FileUtils.write2File(formatted, FileUtils.getConfigV2File());
        }
        init = true;
        return INSTANCE;
    }

}
