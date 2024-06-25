package tkaxv7s.xposed.sesame.data;

import java.util.HashMap;
import java.util.Map;

public enum ModelType {

    NORMAL(0, "普通模块"),

    TASK(1, "任务模块"),

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
