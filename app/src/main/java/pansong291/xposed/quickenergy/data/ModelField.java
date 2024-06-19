package pansong291.xposed.quickenergy.data;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import lombok.Data;
import pansong291.xposed.quickenergy.R;

@Data
public class ModelField implements Serializable {

    private static final String extPackage = "pansong291.xposed.quickenergy.data.modelFieldExt.";

    private String type;

    @JsonIgnore
    private String code;

    @JsonIgnore
    private String name;

    protected volatile Object value;

    @JsonIgnore
    protected Object defaultValue;

    public ModelField() {
        this.type = this.getClass().getName().replace(extPackage, "");
    }

    public ModelField(Object value) {
        this();
        this.defaultValue = value;
        setValue(value);
    }

    public ModelField(Object value, Object defaultValue) {
        this();
        this.defaultValue = defaultValue;
        setValue(value);
    }

    public ModelField(String code, String name, Object value) {
        this();
        this.code = code;
        this.name = name;
        this.defaultValue = value;
        setValue(value);
    }

    @JsonIgnore
    public String getConfigValue() {
        return String.valueOf(getValue());
    }

    public void setConfigValue(String value) {
        setValue(value);
    }

    @JsonIgnore
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#008175"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> {
            Toast toast = Toast.makeText(context, "无配置项", Toast.LENGTH_SHORT);
            toast.setGravity(toast.getGravity(), toast.getXOffset(), ConfigV2.INSTANCE.getToastOffsetY());
            toast.show();
        });
        return btn;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Class<? extends ModelField> getClazz() {
        try {
            return (Class<? extends ModelField>) Class.forName(extPackage + type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelField clone() {
        try {
            ModelField modelField = getClazz().newInstance();
            modelField.setCode(code);
            modelField.setName(name);
            modelField.setValue(value);
            return modelField;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

}
