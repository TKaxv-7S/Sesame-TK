package pansong291.xposed.quickenergy.data;

import java.util.LinkedHashMap;

//@Data
public final class ModelFields extends LinkedHashMap<String, ModelField> {

    //private BooleanModelField enable = new BooleanModelField("enable", "开启", true);

    public void addField(ModelField modelField) {
        put(modelField.getCode(), modelField);
    }
}