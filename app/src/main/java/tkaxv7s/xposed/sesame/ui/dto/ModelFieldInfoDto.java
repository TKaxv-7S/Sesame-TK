package tkaxv7s.xposed.sesame.ui.dto;

import lombok.Data;
import tkaxv7s.xposed.sesame.data.ModelField;

import java.io.Serializable;

@Data
public class ModelFieldInfoDto implements Serializable {

    private String code;

    private String name;

    private String type;

    private Object expandKey;

    private Object expandValue;

    private String configValue;

    public ModelFieldInfoDto() {
    }

    public static ModelFieldInfoDto toInfoDto(ModelField<?> modelField) {
        ModelFieldInfoDto dto = new ModelFieldInfoDto();
        dto.setCode(modelField.getCode());
        dto.setName(modelField.getName());
        dto.setType(modelField.getType());
        dto.setExpandKey(modelField.getExpandKey());
        dto.setExpandValue(modelField.getExpandValue());
        dto.setConfigValue(modelField.getConfigValue());
        return dto;
    }

}
