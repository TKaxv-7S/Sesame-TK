package tkaxv7s.xposed.sesame.ui.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ModelDto implements Serializable {

    private String modelCode;

    private String modelName;

    public ModelDto() {
    }

    public ModelDto(String modelCode, String modelName) {
        this.modelCode = modelCode;
        this.modelName = modelName;
    }
}
