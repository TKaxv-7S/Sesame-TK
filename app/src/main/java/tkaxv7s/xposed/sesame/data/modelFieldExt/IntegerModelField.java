package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import lombok.Getter;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.ui.StringDialog;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.List;

@Getter
public class IntegerModelField extends ModelField<Integer> {

    protected final Integer minLimit;

    protected final Integer maxLimit;

    public IntegerModelField(String code, String name, Integer value) {
        super(code, name, value);
        this.minLimit = null;
        this.maxLimit = null;
    }

    public IntegerModelField(String code, String name, Integer value, Integer minLimit, Integer maxLimit) {
        super(code, name, value);
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
    }

    @Override
    public String getType() {
        return "INTEGER";
    }

    @Override
    public void setObjectValue(Object value) {
        Integer newValue;
        if (value == null) {
            newValue = defaultValue;
        } else {
            try {
                newValue = Integer.parseInt(value.toString());
            } catch (Exception e) {
                Log.printStackTrace(e);
                newValue = defaultValue;
            }
        }
        if (minLimit != null) {
            newValue = Math.max(minLimit, newValue);
        }
        if (maxLimit != null) {
            newValue = Math.min(maxLimit, newValue);
        }
        this.value = newValue;
    }

    @Override
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#216EEE"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> StringDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    @Getter
    public static class MultiplyIntegerModelField extends IntegerModelField {

        private final Integer multiple;

        public MultiplyIntegerModelField(String code, String name, Integer value, Integer minLimit, Integer maxLimit, Integer multiple) {
            super(code, name, value * multiple, minLimit * multiple, maxLimit * multiple);
            this.multiple = multiple;
        }

        @Override
        public String getType() {
            return "MULTIPLY_INTEGER";
        }

        @Override
        public Object fromConfigValue(String configValue) {
            if (configValue == null) {
                return null;
            }
            try {
                return Integer.parseInt(configValue) * multiple;
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
            return null;
        }

        @Override
        public String toConfigValue(Integer value) {
            return String.valueOf(value / multiple);
        }

    }

}
