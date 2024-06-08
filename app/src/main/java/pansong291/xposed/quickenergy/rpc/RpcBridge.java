package pansong291.xposed.quickenergy.rpc;

import java.util.function.Function;

public interface RpcBridge {

    void load();

    void unload();

    default String requestJson(String method, String data) {
        return requestJson(method, data, 3);
    }

    String requestJson(String method, String data, int retryCount);

    default <T> T requestObj(String method, String data, Function<Object, T> callback) {
        return requestObj(method, data, callback, 3);
    }

    <T> T requestObj(String method, String data, Function<Object, T> callback, int retryCount);

}
