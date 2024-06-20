package tkaxv7s.xposed.sesame.data;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import tkaxv7s.xposed.sesame.task.common.ModelTask;

@Data
public final class ModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    //private final Type dataType;

    private String code;

    private String name;

    private final ModelFields fields = new ModelFields();

    public ModelConfig() {
        //dataType = TypeUtil.getTypeArgument(this.getClass().getGenericSuperclass(), 0);
    }

    public ModelConfig(ModelTask modelTask) {
        this();
        this.code = modelTask.getClass().getSimpleName();
        this.name = modelTask.setName();
        addFields(modelTask.setFields());
    }

    public void addFields(ModelFields newFields) {
        fields.clear();
        if (newFields != null) {
            for (Map.Entry<String, ModelField> entry : newFields.entrySet()) {
                ModelField modelField = entry.getValue();
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
    public <T extends ModelField> T getModelFieldExt(String fieldCode) {
        return (T) fields.get(fieldCode);
    }
}
