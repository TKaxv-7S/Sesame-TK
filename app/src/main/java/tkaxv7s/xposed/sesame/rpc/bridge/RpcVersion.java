package tkaxv7s.xposed.sesame.rpc.bridge;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum RpcVersion {

    OLD("OLD"),
    NEW("NEW"),

    ;

    final String code;

    RpcVersion(String code) {
        this.code = code;
    }

    private static final Map<String, RpcVersion> MAP;

    static {
        MAP = new HashMap<>();
        RpcVersion[] values = RpcVersion.values();
        for (RpcVersion value : values) {
            MAP.put(value.code, value);
        }
    }

    public static RpcVersion getByCode(String code) {
        return MAP.get(code);
    }

}