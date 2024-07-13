package tkaxv7s.xposed.sesame.data;

import lombok.Data;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;

import java.io.Serializable;
import java.util.Map;

@Data
public final class ModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    //private final Type dataType;

    private String code;

    private String name;

    private ModelGroup group;

    private String icon;

    private final ModelFields fields = new ModelFields();

    public ModelConfig() {
        //dataType = TypeUtil.getTypeArgument(this.getClass().getGenericSuperclass(), 0);
    }

    public ModelConfig(Model model) {
        this();
        this.code = model.getClass().getSimpleName();
        this.name = model.getName();
        this.group = model.getGroup();
        this.icon = model.getIcon();
        BooleanModelField enableField = model.getEnableField();
        fields.put(enableField.getCode(), enableField);
        ModelFields modelFields = model.getFields();
        if (modelFields != null) {
            for (Map.Entry<String, ModelField<?>> entry : modelFields.entrySet()) {
                ModelField<?> modelField = entry.getValue();
                if (modelField != null) {
                    fields.put(modelField.getCode(), modelField);
                }
            }
        }
    }

    public Boolean hasModelField(String fieldCode) {
        return fields.containsKey(fieldCode);
    }

    public ModelField getModelField(String fieldCode) {
        return fields.get(fieldCode);
    }

    /*public void removeModelField(String fieldCode) {
        fields.remove(fieldCode);
    }*/

    /*public Boolean addModelField(ModelField modelField) {
        fields.put(modelField.getCode(), modelField);
        return true;
    }*/

    @SuppressWarnings("unchecked")
    public <T extends ModelField<?>> T getModelFieldExt(String fieldCode) {
        return (T) fields.get(fieldCode);
    }
}
