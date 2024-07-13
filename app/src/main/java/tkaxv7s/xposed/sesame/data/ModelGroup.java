package tkaxv7s.xposed.sesame.data;

import java.util.HashMap;
import java.util.Map;

public enum ModelGroup {

    BASE("BASE", "基础")
    , FOREST("FOREST", "森林")
    , FARM("FARM", "庄园")
    , STALL("STALL", "新村")
    , ORCHARD("ORCHARD", "农场")
    , SPORTS("SPORTS", "运动")
    , MEMBER("MEMBER", "会员")
    , OTHER("OTHER", "其他")

    ;


    final String code;

    final String name;

    ModelGroup(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Map<String, ModelGroup> MAP;

    static {
        MAP = new HashMap<>();
        ModelGroup[] values = ModelGroup.values();
        for (ModelGroup value : values) {
            MAP.put(value.code, value);
        }
    }

    public static ModelGroup getByCode(String code) {
        return MAP.get(code);
    }

    public static String getName(String code) {
        ModelGroup modelGroup = getByCode(code);
        if (modelGroup == null) {
            return null;
        }
        return modelGroup.name;
    }

}
