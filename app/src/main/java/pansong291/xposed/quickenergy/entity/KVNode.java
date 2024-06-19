package pansong291.xposed.quickenergy.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class KVNode<K, V> implements Serializable {

    private K key;

    private V value;

    public KVNode() {
    }

    public KVNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

}