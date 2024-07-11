package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.ui.ChoiceDialog;
import tkaxv7s.xposed.sesame.util.JsonUtil;

public class ChoiceModelField extends ModelField {

    private String[] choiceArray;

    public ChoiceModelField(String code, String name, Integer value) {
        super(code, name, value);
    }

    public ChoiceModelField(String code, String name, Integer value, String[] choiceArray) {
        super(code, name, value);
        this.choiceArray = choiceArray;
    }

    @Override
    public String getType() {
        return "CHOICE";
    }

    public String[] getExpandKey() {
        return choiceArray;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, Integer.class);
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
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
        btn.setOnClickListener(v -> ChoiceDialog.show(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

}
