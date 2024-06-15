package pansong291.xposed.quickenergy.data.modelFieldExt;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class ListModelField extends ModelField {

    private static final TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {
    };

    public ListModelField() {
    }

    public ListModelField(String code, String name, List<String> value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
    public List<String> getValue() {
        return (List<String>) value;
    }

}
