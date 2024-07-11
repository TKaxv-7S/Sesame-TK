package tkaxv7s.xposed.sesame.ui.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ModelDto implements Serializable {

    private String modelCode;

    private String modelName;

    private String modelIcon;

    public ModelDto() {
    }

    public ModelDto(String modelCode, String modelName, String modelIcon) {
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelIcon = modelIcon;
    }
}
