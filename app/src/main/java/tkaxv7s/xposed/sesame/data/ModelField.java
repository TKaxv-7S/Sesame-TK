package tkaxv7s.xposed.sesame.data;

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
import tkaxv7s.xposed.sesame.R;

@Data
public class ModelField implements Serializable {

    private static final String extPackage = "tkaxv7s.xposed.sesame.data.modelFieldExt.";

    @JsonIgnore
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
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> {
            Toast.makeText(context, "无配置项", Toast.LENGTH_SHORT).show();
        });
        return btn;
    }

}
