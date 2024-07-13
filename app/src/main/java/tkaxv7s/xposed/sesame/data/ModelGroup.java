package tkaxv7s.xposed.sesame.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ModelGroup {

    BASE("BASE", "基础", null)
    , FOREST("FOREST", "森林", null)
    , FARM("FARM", "庄园", null)
    , STALL("STALL", "新村", null)
    , ORCHARD("ORCHARD", "农场", null)
    , SPORTS("SPORTS", "运动", null)
    , MEMBER("MEMBER", "会员", null)
    , OTHER("OTHER", "其他", null)

    ;

    final String code;

    final String name;

    final String icon;

    ModelGroup(String code, String name, String icon) {
        this.code = code;
        this.name = name;
        this.icon = icon;
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
