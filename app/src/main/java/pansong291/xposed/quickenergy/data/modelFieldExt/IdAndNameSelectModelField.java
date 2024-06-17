package pansong291.xposed.quickenergy.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.entity.AlipayBeach;
import pansong291.xposed.quickenergy.entity.AlipayReserve;
import pansong291.xposed.quickenergy.entity.AlipayUser;
import pansong291.xposed.quickenergy.entity.AreaCode;
import pansong291.xposed.quickenergy.entity.CooperateUser;
import pansong291.xposed.quickenergy.entity.IdAndName;
import pansong291.xposed.quickenergy.ui.ListDialog;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class IdAndNameSelectModelField extends ModelField {

    private static final TypeReference<KVNode<LinkedHashMap<String, Integer>, Boolean>> typeReference = new TypeReference<KVNode<LinkedHashMap<String, Integer>, Boolean>>() {
    };

    public IdAndNameSelectModelField() {
    }

    public IdAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
        super(code, name, value);
    }

    @JsonIgnore
    public List<? extends IdAndName> getList() {
        return new ArrayList<>();
    }

    @Override
    public void setValue(Object value) {
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
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> ListDialog.show(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class BeachAndNameSelectModelField extends IdAndNameSelectModelField {

        public BeachAndNameSelectModelField() {
        }

        public BeachAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AlipayBeach.getList();
        }
    }

    public static class UserAndNameSelectModelField extends IdAndNameSelectModelField {

        public UserAndNameSelectModelField() {
        }

        public UserAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AlipayUser.getList();
        }
    }

    public static class CooperateUserAndNameSelectModelField extends IdAndNameSelectModelField {

        public CooperateUserAndNameSelectModelField() {
        }

        public CooperateUserAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return CooperateUser.getList();
        }
    }

    public static class AreaCodeAndNameSelectModelField extends IdAndNameSelectModelField {

        public AreaCodeAndNameSelectModelField() {
        }

        public AreaCodeAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AreaCode.getList();
        }
    }

    public static class ReserveAndNameSelectModelField extends IdAndNameSelectModelField {

        public ReserveAndNameSelectModelField() {
        }

        public ReserveAndNameSelectModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AlipayReserve.getList();
        }
    }

    public static class UserAndNameSelectOneModelField extends IdAndNameSelectModelField {

        public UserAndNameSelectOneModelField() {
        }

        public UserAndNameSelectOneModelField(String code, String name, KVNode<Map<String, Integer>, Boolean> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AlipayUser.getList();
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

    @Data
    public static class KVNode<K, V> implements Serializable {

        private K key;

        private V value;

        public KVNode() {
        }

        public KVNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
