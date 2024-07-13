package tkaxv7s.xposed.sesame.ui.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ModelDto implements Serializable {

    private String modelCode;

    private String modelName;

    private String groupCode;

    private List<ModelFieldShowDto> modelFields;

    public ModelDto() {
    }

    public ModelDto(String modelCode, String modelName, String groupCode, List<ModelFieldShowDto> modelFields) {
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.groupCode = groupCode;
        this.modelFields = modelFields;
    }

}
