package tkaxv7s.xposed.sesame.data;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum RunType {

    DISABLE(0, "已关闭"),

    MODEL(1, "已激活"),

    PACKAGE(2, "已加载"),

    ;

    private final Integer code;

    private final String name;

    RunType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Map<Integer, RunType> MAP;

    static {
        MAP = new HashMap<>();
        RunType[] values = RunType.values();
        for (RunType value : values) {
            MAP.put(value.code, value);
        }
    }

    public static RunType getByCode(Integer code) {
        return MAP.get(code);
    }

}