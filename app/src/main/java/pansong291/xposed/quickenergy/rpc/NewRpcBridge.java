package pansong291.xposed.quickenergy.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import pansong291.xposed.quickenergy.entity.RpcEntity;
import pansong291.xposed.quickenergy.hook.ApplicationHook;
import pansong291.xposed.quickenergy.hook.Notification;
import pansong291.xposed.quickenergy.util.ClassUtil;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;

/**
 * 新版rpc接口 支持最低支付宝版本v10.3.96.8100
 * 记录rpc抓包 支持最低支付宝版本v10.3.96.8100
 */
public class NewRpcBridge implements RpcBridge {

    private static final String TAG = NewRpcBridge.class.getSimpleName();

    private ClassLoader loader;

    private Object newRpcInstance;

    private Method parseObjectMethod;

    private Class<?>[] bridgeCallbackClazzArray;

    private Method newRpcCallMethod;


    @Override
    public void load() {
        loader = ApplicationHook.getClassLoader();
        try {
            Object service = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.nebulacore.Nebula", loader), "getService");
            Object extensionManager = XposedHelpers.callMethod(service, "getExtensionManager");
            Method getExtensionByName = extensionManager.getClass().getDeclaredMethod("createExtensionInstance", Class.class);
            getExtensionByName.setAccessible(true);
            newRpcInstance = getExtensionByName.invoke(null, loader.loadClass("com.alibaba.ariver.commonability.network.rpc.RpcBridgeExtension"));
            if (newRpcInstance == null) {
                Object nodeExtensionMap = XposedHelpers.callMethod(extensionManager, "getNodeExtensionMap");
                if (nodeExtensionMap != null) {
                    Map<Object, Map<String, Object>> map = (Map<Object, Map<String, Object>>) nodeExtensionMap;
                    for (Map.Entry<Object, Map<String, Object>> entry : map.entrySet()) {
                        Map<String, Object> map1 = entry.getValue();
                        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
                            if ("com.alibaba.ariver.commonability.network.rpc.RpcBridgeExtension".equals(entry1.getKey())) {
                                newRpcInstance = entry1.getValue();
                                break;
                            }
                        }
                    }
                }
                if (newRpcInstance == null) {
                    Log.i(TAG, "get newRpcInstance null");
                    throw new RuntimeException("get newRpcInstance is null");
                }
            }
            parseObjectMethod = loader.loadClass("com.alibaba.fastjson.JSON").getMethod("parseObject", String.class);
            Class<?> bridgeCallbackClazz = loader.loadClass("com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback");
            bridgeCallbackClazzArray = new Class[]{bridgeCallbackClazz};
            newRpcCallMethod = newRpcInstance.getClass().getMethod("rpc"
                    , String.class
                    , boolean.class
                    , boolean.class
                    , String.class
                    , loader.loadClass(ClassUtil.JSON_OBJECT_NAME)
                    , String.class
                    , loader.loadClass(ClassUtil.JSON_OBJECT_NAME)
                    , boolean.class
                    , boolean.class
                    , int.class
                    , boolean.class
                    , String.class
                    , loader.loadClass("com.alibaba.ariver.app.api.App")
                    , loader.loadClass("com.alibaba.ariver.app.api.Page")
                    , loader.loadClass("com.alibaba.ariver.engine.api.bridge.model.ApiContext")
                    , bridgeCallbackClazz
            );
            Log.i(TAG, "get newRpcCallMethod successfully");
        } catch (Exception e) {
            Log.i(TAG, "get newRpcCallMethod err:");
            Log.printStackTrace(TAG, e);
        }
    }

    @Override
    public void unload() {
        newRpcCallMethod = null;
        bridgeCallbackClazzArray = null;
        parseObjectMethod = null;
        newRpcInstance = null;
        loader = null;
    }

    public String requestString(RpcEntity rpcEntity, int retryCount) {
        RpcEntity resRpcEntity = requestObject(rpcEntity, retryCount);
        if (resRpcEntity != null) {
            return resRpcEntity.getResponseString();
        }
        return null;
    }

    @Override
    public RpcEntity requestObject(RpcEntity rpcEntity, int retryCount) {
        if (ApplicationHook.isOffline()) {
            return null;
        }
        String method = rpcEntity.getRequestMethod();
        String data = rpcEntity.getRequestData();
        int count = 0;
        do {
            count++;
            try {
                Log.i(TAG, "new rpc request: " + method + ", " + data);
                newRpcCallMethod.invoke(
                        newRpcInstance, method, false, false, "json", parseObjectMethod.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + method + "\",\"requestData\":" + data + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, bridgeCallbackClazzArray, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) {
                                if (args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                    try {
                                        Object obj = args[0];
                                        if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                            rpcEntity.setError();
                                        }
                                        String result = (String) XposedHelpers.callMethod(obj, "toJSONString");
                                        rpcEntity.setResponseObject(obj, result);
                                        Log.i(TAG, "new rpc response: " + result);
                                    } catch (Exception e) {
                                        rpcEntity.setError();
                                        Log.i(TAG, "new rpc response [" + method + "] err:");
                                        Log.printStackTrace(TAG, e);
                                    }
                                }
                                return null;
                            }
                        })
                );
                if (!rpcEntity.getHasResult()) {
                    return null;
                }
                if (!rpcEntity.getHasError()) {
                    return rpcEntity;
                }
                try {
                    String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResponseObject(), "getString", "error");
                    if ("2000".equals(errorCode)) {
                        if (!ApplicationHook.isOffline()) {
                            ApplicationHook.setOffline(true);
                            Notification.setContentText("登录超时");
                            if (Config.INSTANCE.isTimeoutRestart()) {
                                Log.record("尝试重新登录");
                                ApplicationHook.reLoginByBroadcast();
                            }
                        }
                        return null;
                    }
                    return rpcEntity;
                } catch (Exception e) {
                    Log.i(TAG, "new rpc response [" + method + "] get err:");
                    Log.printStackTrace(TAG, e);
                }
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    Log.printStackTrace(e);
                }
            } catch (Throwable t) {
                Log.i(TAG, "new rpc request [" + method + "] err:");
                Log.printStackTrace(TAG, t);
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    Log.printStackTrace(e);
                }
            }
        } while (count < retryCount);
        return null;
    }

    public RpcEntity newAsyncRequest(RpcEntity rpcEntity, int retryCount) {
        if (ApplicationHook.isOffline()) {
            return null;
        }
        String method = rpcEntity.getRequestMethod();
        String data = rpcEntity.getRequestData();
        int count = 0;
        do {
            count++;
            try {
                synchronized (rpcEntity) {
                    Log.i(TAG, "new rpc request: " + method + ", " + data);
                    newRpcCallMethod.invoke(
                            newRpcInstance, method, false, false, "json", parseObjectMethod.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + method + "\",\"requestData\":" + data + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, bridgeCallbackClazzArray, new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) {
                                    if (args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                        try {
                                            synchronized (rpcEntity) {
                                                Object obj = args[0];
                                                if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                                    rpcEntity.setError();
                                                }
                                                String result = (String) XposedHelpers.callMethod(obj, "toJSONString");
                                                rpcEntity.setResponseObject(obj, result);
                                                Log.i(TAG, "new rpc response: " + result);
                                                Thread thread = rpcEntity.getRequestThread();
                                                if (thread != null) {
                                                    rpcEntity.notifyAll();
                                                }
                                            }
                                        } catch (Exception e) {
                                            rpcEntity.setError();
                                            Log.i(TAG, "new rpc response [" + method + "] err:");
                                            Log.printStackTrace(TAG, e);
                                            synchronized (rpcEntity) {
                                                Thread thread = rpcEntity.getRequestThread();
                                                if (thread != null) {
                                                    rpcEntity.notifyAll();
                                                }
                                            }
                                        }
                                    }
                                    return null;
                                }
                            })
                    );
                    rpcEntity.wait(30_000);
                }
                if (!rpcEntity.getHasResult()) {
                    return null;
                }
                if (!rpcEntity.getHasError()) {
                    return rpcEntity;
                }
                try {
                    String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResponseObject(), "getString", "error");
                    if ("2000".equals(errorCode)) {
                        if (!ApplicationHook.isOffline()) {
                            ApplicationHook.setOffline(true);
                            Notification.setContentText("登录超时");
                            if (Config.INSTANCE.isTimeoutRestart()) {
                                Log.record("尝试重新登录");
                                ApplicationHook.reLoginByBroadcast();
                            }
                        }
                        return null;
                    }
                    return rpcEntity;
                } catch (Exception e) {
                    Log.i(TAG, "new rpc response [" + method + "] get err:");
                    Log.printStackTrace(TAG, e);
                }
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    Log.printStackTrace(e);
                }
            } catch (Throwable t) {
                Log.i(TAG, "new rpc request [" + method + "] err:");
                Log.printStackTrace(TAG, t);
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    Log.printStackTrace(e);
                }
            }
        } while (count < retryCount);
        return null;
    }

}
