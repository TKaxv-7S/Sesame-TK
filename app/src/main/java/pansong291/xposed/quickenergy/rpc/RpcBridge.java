package pansong291.xposed.quickenergy.rpc;

import pansong291.xposed.quickenergy.entity.RpcEntity;

public interface RpcBridge {

    void load() throws Exception;

    void unload();

    String requestString(RpcEntity rpcEntity, int retryCount);

    default String requestString(RpcEntity rpcEntity) {
        return requestString(rpcEntity, 3);
    }

    default String requestString(String method, String data) {
        return requestString(method, data, 3);
    }

    default String requestString(String method, String data, int retryCount) {
        return requestString(new RpcEntity(method, data), retryCount);
    }

    RpcEntity requestObject(RpcEntity rpcEntity, int retryCount);

    default RpcEntity requestObject(RpcEntity rpcEntity) {
        return requestObject(rpcEntity, 3);
    }

    default RpcEntity requestObject(String method, String data) {
        return requestObject(method, data, 3);
    }

    default RpcEntity requestObject(String method, String data, int retryCount) {
        return requestObject(new RpcEntity(method, data), retryCount);
    }

}
