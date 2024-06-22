package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.entity.AlipayBeach;
import tkaxv7s.xposed.sesame.entity.AlipayReserve;
import tkaxv7s.xposed.sesame.entity.AreaCode;
import tkaxv7s.xposed.sesame.entity.CooperateUser;
import tkaxv7s.xposed.sesame.entity.IdAndName;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.ui.ListDialog;
import tkaxv7s.xposed.sesame.util.JsonUtil;

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

    private List<? extends IdAndName> idAndNameList;

    public SelectModelField() {
    }

    public SelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, List<? extends IdAndName> idAndNameList) {
        super(code, name, value);
        this.idAndNameList = idAndNameList;
    }

    @JsonIgnore
    public List<? extends IdAndName> getIdAndNameList() {
        return idAndNameList;
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

    public static class BeachAndNameSelectModelField extends SelectModelField {

        public BeachAndNameSelectModelField() {
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getIdAndNameList() {
            return AlipayBeach.getList();
        }
    }

    public static class CooperateUserAndNameSelectModelField extends SelectModelField {

        public CooperateUserAndNameSelectModelField() {
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getIdAndNameList() {
            return CooperateUser.getList();
        }
    }

    public static class AreaCodeAndNameSelectModelField extends SelectModelField {

        public AreaCodeAndNameSelectModelField() {
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getIdAndNameList() {
            return AreaCode.getList();
        }
    }

    public static class ReserveAndNameSelectModelField extends SelectModelField {

        public ReserveAndNameSelectModelField() {
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getIdAndNameList() {
            return AlipayReserve.getList();
        }
    }

    public static class SelectOneModelField extends SelectModelField {

        public SelectOneModelField() {
        }

        public SelectOneModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value, List<? extends IdAndName> idAndNameList) {
            super(code, name, value, idAndNameList);
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

}
