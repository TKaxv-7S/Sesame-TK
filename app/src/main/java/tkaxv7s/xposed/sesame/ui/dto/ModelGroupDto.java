package tkaxv7s.xposed.sesame.ui.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ModelGroupDto implements Serializable {

    private String code;

    private String name;

    private String icon;

    public ModelGroupDto() {
    }

    public ModelGroupDto(String code, String name, String icon) {
        this.code = code;
        this.name = name;
        this.icon = icon;
    }
}

