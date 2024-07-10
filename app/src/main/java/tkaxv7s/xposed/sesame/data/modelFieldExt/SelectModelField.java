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
import tkaxv7s.xposed.sesame.entity.IdAndName;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.ui.ListDialog;
import tkaxv7s.xposed.sesame.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据结构说明
 * KVNode<Map<String, Integer>, Boolean>
 *     Map<String, Integer> 表示已选择的数据与已经设置的数量映射关系，如果未设置数量，则默认为0
 *     Boolean 表示是否需要设置数量
 * List<? extends IdAndName> 需要选择的数据
 */
public class SelectModelField extends ModelField {

    private static final TypeReference<KVNode<LinkedHashMap<String, Integer>, Boolean>> typeReference = new TypeReference<KVNode<LinkedHashMap<String, Integer>, Boolean>>() {
    };

    private SelectListFunc selectListFunc;

    private List<? extends IdAndName> expandValue;

    public SelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, List<? extends IdAndName> expandValue) {
        super(code, name, value);
        this.expandValue = expandValue;
    }

    public SelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, SelectListFunc selectListFunc) {
        super(code, name, value);
        this.selectListFunc = selectListFunc;
    }

    @Override
    public String getType() {
        return "SELECT";
    }

    public List<? extends IdAndName> getExpandValue() {
        return selectListFunc == null ? expandValue : selectListFunc.getList();
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
    public KVNode<Map<String, Integer>, Boolean> getValue() {
        return (KVNode<Map<String, Integer>, Boolean>) value;
    }

    public String getConfigValue() {
        return JsonUtil.toJsonString(value);
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
        btn.setOnClickListener(v -> ListDialog.show(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class SelectOneModelField extends SelectModelField {

        public SelectOneModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, List<? extends IdAndName> idAndNameList) {
            super(code, name, value, idAndNameList);
        }

        public SelectOneModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, SelectListFunc selectListFunc) {
            super(code, name, value, selectListFunc);
        }

        @Override
        public String getType() {
            return "SELECT_ONE";
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
            btn.setPaddingRelative(40, 0, 40, 0);
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> ListDialog.show(v.getContext(), ((Button) v).getText(), this, ListDialog.ListType.RADIO));
            return btn;
        }
    }

    public interface SelectListFunc {
        List<? extends IdAndName> getList();
    }
}
