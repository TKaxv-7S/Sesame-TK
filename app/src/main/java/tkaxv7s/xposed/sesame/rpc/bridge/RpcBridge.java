package tkaxv7s.xposed.sesame.rpc.bridge;

import tkaxv7s.xposed.sesame.entity.RpcEntity;

public interface RpcBridge {

    void load() throws Exception;

    void unload();

    String requestString(RpcEntity rpcEntity, int tryCount, int retryInterval);

    default String requestString(RpcEntity rpcEntity) {
        return requestString(rpcEntity, 3, -1);
    }

    default String requestString(String method, String data) {
        return requestString(method, data, 3, -1);
    }

    default String requestString(String method, String data, String relation) {
        return requestString(method, data, relation, 3, -1);
    }

    default String requestString(String method, String data, String relation, Long time) {
        return requestString(method, data, relation, time, 3, -1);
    }

    default String requestString(String method, String data, int tryCount, int retryInterval) {
        return requestString(new RpcEntity(method, data), tryCount, retryInterval);
    }

    default String requestString(String method, String data, String relation, int tryCount, int retryInterval) {
        return requestString(new RpcEntity(method, data, relation), tryCount, retryInterval);
    }

    default String requestString(String method, String data, String relation, Long time, int tryCount, int retryInterval) {
        return requestString(new RpcEntity(method, data, relation, time), tryCount, retryInterval);
    }

    RpcEntity requestObject(RpcEntity rpcEntity, int tryCount, int retryInterval);

    default RpcEntity requestObject(RpcEntity rpcEntity) {
        return requestObject(rpcEntity, 3, -1);
    }

    default RpcEntity requestObject(String method, String data) {
        return requestObject(method, data, 3, -1);
    }

    default RpcEntity requestObject(String method, String data, String relation) {
        return requestObject(method, data, relation, 3, -1);
    }

    default RpcEntity requestObject(String method, String data, String relation, Long time) {
        return requestObject(method, data, relation, time, 3, -1);
    }

    default RpcEntity requestObject(String method, String data, int tryCount, int retryInterval) {
        return requestObject(new RpcEntity(method, data), tryCount, retryInterval);
    }

    default RpcEntity requestObject(String method, String data, String relation, int tryCount, int retryInterval) {
        return requestObject(new RpcEntity(method, data, relation), tryCount, retryInterval);
    }

    default RpcEntity requestObject(String method, String data, String relation, Long time, int tryCount, int retryInterval) {
        return requestObject(new RpcEntity(method, data, relation, time), tryCount, retryInterval);
    }

}
