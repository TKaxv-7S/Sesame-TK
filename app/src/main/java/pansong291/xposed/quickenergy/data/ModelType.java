package pansong291.xposed.quickenergy.data;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum ModelType {

    DISABLE(0, "关闭"),

    MODEL(1, "模块"),

    PACKAGE(2, "内置"),

    ;

    private final Integer code;

    private final String name;

    ModelType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Map<Integer, ModelType> MAP;

    static {
        MAP = new HashMap<>();
        ModelType[] values = ModelType.values();
        for (ModelType value : values) {
            MAP.put(value.code, value);
        }
    }

    public static ModelType getByCode(Integer code) {
        return MAP.get(code);
    }

}