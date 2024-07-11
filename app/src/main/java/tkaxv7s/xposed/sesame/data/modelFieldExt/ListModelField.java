package tkaxv7s.xposed.sesame.data.modelFieldExt;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.fasterxml.jackson.core.type.TypeReference;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.ui.StringDialog;
import tkaxv7s.xposed.sesame.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class ListModelField extends ModelField<List<String>> {

    private static final TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {
    };

    public ListModelField(String code, String name, List<String> value) {
        super(code, name, value);
    }

    @Override
    public String getType() {
        return "LIST";
    }

    @Override
    public void setObjectValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
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
        btn.setOnClickListener(v -> StringDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class ListJoinCommaToStringModelField extends ListModelField {

        public ListJoinCommaToStringModelField(String code, String name, List<String> value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(String value) {
            if (value == null) {
                setObjectValue(null);
                return;
            }
            List<String> list = new ArrayList<>();
            String[] split = value.split(",");
            if (split.length == 1) {
                String str = split[0];
                if (!str.isEmpty()) {
                    list.add(str);
                }
            } else {
                for (String str : split) {
                    if (!str.isEmpty()) {
                        list.add(str);
                    }
                }
            }
            setObjectValue(list);
        }

        @Override
        public String getConfigValue() {
            return String.join(",", getValue());
        }
    }

}
