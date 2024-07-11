package tkaxv7s.xposed.sesame.rpc;

import de.robv.android.xposed.XposedHelpers;
import tkaxv7s.xposed.sesame.entity.RpcEntity;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.util.ClassUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.NotificationUtil;
import tkaxv7s.xposed.sesame.util.RandomUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

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
    public void load() throws Exception {
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
            throw e;
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

    public String requestString(RpcEntity rpcEntity, int tryCount, int retryInterval) {
        RpcEntity resRpcEntity = requestObject(rpcEntity, tryCount, retryInterval);
        if (resRpcEntity != null) {
            return resRpcEntity.getResponseString();
        }
        return null;
    }

    @Override
    public RpcEntity requestObject(RpcEntity rpcEntity, int tryCount, int retryInterval) {
        if (ApplicationHook.isOffline()) {
            return null;
        }
        int id = rpcEntity.hashCode();
        String method = rpcEntity.getRequestMethod();
        String data = rpcEntity.getRequestData();
        String relation = rpcEntity.getRequestRelation();
        try {
            int count = 0;
            do {
                count++;
                try {
                    newRpcCallMethod.invoke(
                            newRpcInstance, method, false, false, "json", parseObjectMethod.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + method + "\",\"requestData\":" + data + (relation == null ? "" : ",\"relationLocal\":" + relation) + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, bridgeCallbackClazzArray, new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) {
                                    if (args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                        try {
                                            Object obj = args[0];
                                            if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                                rpcEntity.setError();
                                            }
                                            rpcEntity.setResponseObject(obj, (String) XposedHelpers.callMethod(obj, "toJSONString"));
                                        } catch (Exception e) {
                                            rpcEntity.setError();
                                            Log.i(TAG, "new rpc response | id: " + id + " | method: " + method + " err:");
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
                                NotificationUtil.updateStatusText("登录超时");
                                if (BaseModel.getTimeoutRestart().getValue()) {
                                    Log.record("尝试重新登录");
                                    ApplicationHook.reLoginByBroadcast();
                                }
                            }
                            return null;
                        }
                        return rpcEntity;
                    } catch (Exception e) {
                        Log.i(TAG, "new rpc response | id: " + id + " | method: " + method + " get err:");
                        Log.printStackTrace(TAG, e);
                    }
                    if (retryInterval < 0) {
                        try {
                            Thread.sleep(600 + RandomUtil.delay());
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    } else if (retryInterval > 0) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    }
                } catch (Throwable t) {
                    Log.i(TAG, "new rpc request | id: " + id + " | method: " + method + " err:");
                    Log.printStackTrace(TAG, t);
                    if (retryInterval < 0) {
                        try {
                            Thread.sleep(600 + RandomUtil.delay());
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    } else if (retryInterval > 0) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    }
                }
            } while (count < tryCount);
            return null;
        } finally {
            Log.i("New RPC\n方法: " + method + "\n参数: " + data + "\n数据: " + rpcEntity.getResponseString() + "\n");
        }
    }

    public RpcEntity newAsyncRequest(RpcEntity rpcEntity, int tryCount, int retryInterval) {
        if (ApplicationHook.isOffline()) {
            return null;
        }
        int id = rpcEntity.hashCode();
        String method = rpcEntity.getRequestMethod();
        String data = rpcEntity.getRequestData();
        try {
            int count = 0;
            do {
                count++;
                try {
                    synchronized (rpcEntity) {
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
                                                    Thread thread = rpcEntity.getRequestThread();
                                                    if (thread != null) {
                                                        rpcEntity.notifyAll();
                                                    }
                                                }
                                            } catch (Exception e) {
                                                rpcEntity.setError();
                                                Log.i(TAG, "new rpc response | id: " + id + " | method: " + method + " err:");
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
                                NotificationUtil.updateStatusText("登录超时");
                                if (BaseModel.getTimeoutRestart().getValue()) {
                                    Log.record("尝试重新登录");
                                    ApplicationHook.reLoginByBroadcast();
                                }
                            }
                            return null;
                        }
                        return rpcEntity;
                    } catch (Exception e) {
                        Log.i(TAG, "new rpc response | id: " + id + " | method: " + method + " get err:");
                        Log.printStackTrace(TAG, e);
                    }
                    if (retryInterval < 0) {
                        try {
                            Thread.sleep(600 + RandomUtil.delay());
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    } else if (retryInterval > 0) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    }
                } catch (Throwable t) {
                    Log.i(TAG, "new rpc request | id: " + id + " | method: " + method + " err:");
                    Log.printStackTrace(TAG, t);
                    if (retryInterval < 0) {
                        try {
                            Thread.sleep(600 + RandomUtil.delay());
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    } else if (retryInterval > 0) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    }
                }
            } while (count < tryCount);
            return null;
        } finally {
            Log.i("New RPC\n方法: " + method + "\n参数: " + data + "\n数据: " + rpcEntity.getResponseString() + "\n");
        }
    }

}
