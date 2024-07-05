package tkaxv7s.xposed.sesame.data;

import lombok.Getter;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.model.base.ModelOrder;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Model {

    private static final Map<String, ModelConfig> modelConfigMap = new LinkedHashMap<>();

    private static final Map<String, ModelConfig> readOnlyModelConfigMap = Collections.unmodifiableMap(modelConfigMap);

    private static final Map<Class<? extends Model>, Model> modelMap = new ConcurrentHashMap<>();

    private static final List<Class<Model>> modelClazzList = ModelOrder.getClazzList();

    @Getter
    private static final Model[] modelArray = new Model[modelClazzList.size()];

    private static final List<Model> modelList = new LinkedList<>(Arrays.asList(modelArray));

    private static final List<Model> readOnlyModelList = Collections.unmodifiableList(modelList);

    private final BooleanModelField enableField;

    public final BooleanModelField getEnableField() {
        return enableField;
    }

    public Model() {
        this.enableField = new BooleanModelField("enable", getEnableFieldName(), false);
    }

    public String getEnableFieldName() {
        return "开启" + getName();
    }

    public final Boolean isEnable() {
        return enableField.getValue();
    }

    public ModelType getType() {
        return ModelType.NORMAL;
    }

    public abstract String getName();

    public abstract ModelFields getFields();

    public void config(ClassLoader classLoader) {}

    public void destroy() {}

    public static Map<String, ModelConfig> getModelConfigMap() {
        return readOnlyModelConfigMap;
    }

    public static Boolean hasModel(Class<? extends Model> modelClazz) {
        return modelMap.containsKey(modelClazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Model> T getModel(Class<T> modelClazz) {
        return (T) modelMap.get(modelClazz);
    }

    public static List<Model> getModelList() {
        return readOnlyModelList;
    }

    public static synchronized void initAllModel(ClassLoader classLoader) {
        destroyAllModel();
        for (int i = 0, len = modelClazzList.size(); i < len; i++) {
            Class<Model> modelClazz = modelClazzList.get(i);
            try {
                Model model = modelClazz.newInstance();
                ModelConfig modelConfig = new ModelConfig(model);
                modelArray[i] = model;
                modelMap.put(modelClazz, model);
                modelConfigMap.put(modelConfig.getCode(), modelConfig);
            } catch (IllegalAccessException | InstantiationException e) {
                Log.printStackTrace(e);
            }
        }
        for (Model model : modelArray) {
            try {
                if (model.getEnableField().getValue()) {
                    model.config(classLoader);
                }
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    public static synchronized void destroyAllModel() {
        for (int i = 0, len = modelArray.length; i < len; i++) {
            Model model = modelArray[i];
            if (model != null) {
                try {
                    if (ModelType.TASK == model.getType()) {
                        ((ModelTask) model).stopTask();
                    }
                    model.destroy();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
                modelArray[i] = null;
            }
            modelMap.clear();
            modelConfigMap.clear();
        }
    }

}
