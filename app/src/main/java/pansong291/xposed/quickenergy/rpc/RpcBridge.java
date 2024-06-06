package pansong291.xposed.quickenergy.rpc;

import java.util.function.Function;

public interface RpcBridge {

    void load();

    void unload();

    String requestJson(String method, String data);

    <T> T requestObj(String method, String data, Function<Object, T> callback);

}
