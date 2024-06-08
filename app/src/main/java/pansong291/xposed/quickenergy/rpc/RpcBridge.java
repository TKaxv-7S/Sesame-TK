package pansong291.xposed.quickenergy.rpc;

import pansong291.xposed.quickenergy.entity.RpcEntity;

public interface RpcBridge {

    void load();

    void unload();

    default String requestString(String method, String data) {
        return requestString(method, data, 3);
    }

    String requestString(String method, String data, int retryCount);

    default RpcEntity requestObject(String method, String data) {
        return requestObject(method, data, 3);
    }

    RpcEntity requestObject(String method, String data, int retryCount);

}
