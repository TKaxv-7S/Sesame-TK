package tkaxv7s.xposed.sesame.ui.dto;

import java.util.LinkedHashMap;

public final class ModelFieldsDto extends LinkedHashMap<String, ModelFieldShowDto> {

    public void addField(ModelFieldShowDto modelField) {
        put(modelField.getCode(), modelField);
    }

}