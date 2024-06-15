package pansong291.xposed.quickenergy.data.modelFieldExt;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.entity.AlipayBeach;
import pansong291.xposed.quickenergy.entity.AlipayReserve;
import pansong291.xposed.quickenergy.entity.AlipayUser;
import pansong291.xposed.quickenergy.entity.AreaCode;
import pansong291.xposed.quickenergy.entity.CooperateUser;
import pansong291.xposed.quickenergy.entity.IdAndName;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class IdAndNameSelectModelField extends ModelField {

    private static final TypeReference<KVNode<List<String>, List<Integer>>> typeReference = new TypeReference<KVNode<List<String>, List<Integer>>>() {
    };

    public IdAndNameSelectModelField() {
    }

    public IdAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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
    public KVNode<List<String>, List<Integer>> getValue() {
        return (KVNode<List<String>, List<Integer>>) value;
    }

    public static class BeachAndNameSelectModelField extends IdAndNameSelectModelField {

        public BeachAndNameSelectModelField() {
        }

        public BeachAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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

        public UserAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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

        public CooperateUserAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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

        public AreaCodeAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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

        public ReserveAndNameSelectModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
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

        public UserAndNameSelectOneModelField(String code, String name, KVNode<List<String>, List<Integer>> value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public List<? extends IdAndName> getList() {
            return AlipayUser.getList();
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
