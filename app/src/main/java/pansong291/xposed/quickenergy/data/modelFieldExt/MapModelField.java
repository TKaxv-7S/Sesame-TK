package pansong291.xposed.quickenergy.data.modelFieldExt;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class MapModelField extends ModelField {

    private static final TypeReference<Map<String, Integer>> typeReference = new TypeReference<Map<String, Integer>>() {
    };

    public MapModelField() {
    }

    public MapModelField(String code, String name, Map<String, Integer> value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
    public Map<String, Integer> getValue() {
        return (Map<String, Integer>) value;
    }

}
