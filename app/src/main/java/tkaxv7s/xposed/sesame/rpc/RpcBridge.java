package tkaxv7s.xposed.sesame.rpc;

import tkaxv7s.xposed.sesame.entity.RpcEntity;

public interface RpcBridge {

    void load() throws Exception;

    void unload();

    String requestString(RpcEntity rpcEntity, int tryCount, int sleepTime);

    default String requestString(RpcEntity rpcEntity) {
        return requestString(rpcEntity, 3, -1);
    }

    default String requestString(String method, String data) {
        return requestString(method, data, 3, -1);
    }

    default String requestString(String method, String data, int tryCount, int sleepTime) {
        return requestString(new RpcEntity(method, data), tryCount, sleepTime);
    }

    RpcEntity requestObject(RpcEntity rpcEntity, int tryCount, int sleepTime);

    default RpcEntity requestObject(RpcEntity rpcEntity) {
        return requestObject(rpcEntity, 3, -1);
    }

    default RpcEntity requestObject(String method, String data) {
        return requestObject(method, data, 3, -1);
    }

    default RpcEntity requestObject(String method, String data, int tryCount, int sleepTime) {
        return requestObject(new RpcEntity(method, data), tryCount, sleepTime);
    }

}
