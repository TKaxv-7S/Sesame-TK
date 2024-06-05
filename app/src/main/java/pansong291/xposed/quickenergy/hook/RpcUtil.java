package pansong291.xposed.quickenergy.hook;

import org.json.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import pansong291.xposed.quickenergy.AntForestNotification;
import pansong291.xposed.quickenergy.data.RuntimeInfo;
import pansong291.xposed.quickenergy.entity.RpcEntity;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;
import pansong291.xposed.quickenergy.util.StringUtil;

/**
 * 新版rpc接口 支持最低支付宝版本v10.3.96.8100
 * 记录rpc抓包 支持最低支付宝版本v10.3.96.8100
 */
public class RpcUtil {

    private static final String TAG = RpcUtil.class.getSimpleName();

    private static ClassLoader loader;

    private static Class<?> jsonObjectClazz;
    private static Method jsonParse;
    private static Class<?> bridgeCallbackClazz;
    private static Class<?> h5PageClazz;


    private static Method rpcCallMethod;
    private static Method getResponseMethod;
    private static Object curH5PageImpl;


    private static Object newRpcInstance;
    private static Method newRpcCallMethod;


    public static void initClazz() {
        loader = XposedHook.getClassLoader();
        try {
            jsonObjectClazz = loader.loadClass("com.alibaba.fastjson.JSONObject");
            Class<?> jsonClazz = loader.loadClass("com.alibaba.fastjson.JSON");
            jsonParse = jsonClazz.getMethod("parseObject", String.class);
            bridgeCallbackClazz = loader.loadClass("com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback");
            h5PageClazz = loader.loadClass("com.alipay.mobile.h5container.api.H5Page");
            Log.i(TAG, "loadClass successfully");
        } catch (Throwable t) {
            Log.i(TAG, "loadClass err:");
            Log.printStackTrace(TAG, t);
        }
    }


    public static void initMethod() {
        if (rpcCallMethod == null) {
            try {
                rpcCallMethod = loader.loadClass("com.alipay.mobile.nebulaappproxy.api.rpc.H5RpcUtil").getMethod(
                        "rpcCall", String.class, String.class, String.class,
                        boolean.class, jsonObjectClazz, String.class, boolean.class, h5PageClazz,
                        int.class, String.class, boolean.class, int.class, String.class);
                getResponseMethod = loader.loadClass("com.alipay.mobile.nebulaappproxy.api.rpc.H5Response").getMethod("getResponse");
                Log.i(TAG, "get rpcCallMethod successfully");
            } catch (Throwable t) {
                Log.i(TAG, "get rpcCallMethod err:");
                Log.printStackTrace(TAG, t);
            }
        }

        if (newRpcInstance == null) {
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
                newRpcCallMethod = newRpcInstance.getClass().getMethod("rpc"
                        , String.class
                        , boolean.class
                        , boolean.class
                        , String.class
                        , jsonObjectClazz
                        , String.class
                        , jsonObjectClazz
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
                Log.i(TAG, android.util.Log.getStackTraceString(e));
            }

        }
    }

    public static Class<?> getJsonObjectClazz() {
        return jsonObjectClazz;
    }

    public static Class<?> getH5PageClazz() {
        return h5PageClazz;
    }

    public static Object getMicroApplicationContext(ClassLoader classLoader) {
        return XposedHelpers.callMethod(
                XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication", classLoader),
                        "getInstance"), "getMicroApplicationContext");
    }

    public static String getUserId() {
        try {
            ClassLoader classLoader = XposedHook.getClassLoader();
            Object callMethod =
                    XposedHelpers.callMethod(XposedHelpers.callMethod(getMicroApplicationContext(classLoader),
                            "findServiceByInterface",
                            XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService",
                                    classLoader).getName()), "getMyAccountInfoModelByLocal");
            if  (callMethod != null) {
                return (String) XposedHelpers.getObjectField(callMethod, "userId");
            }
        } catch (Throwable th) {
            Log.i(TAG, "getUserId err");
            Log.printStackTrace(TAG, th);
        }
        return null;
    }

    public static String request(String args0, String args1) {
        return newRequest(args0, args1, 3);
    }

    public static String request(String args0, String args1, int retryCount) {
        if (XposedHook.getIsOffline()) {
            return null;
        }
        String result;
        int count = 0;
        do {
            count++;
            try {
                String str = getResponse(doRequest(args0, args1));
                try {
                    JSONObject jo = new JSONObject(str);
                    if (jo.optString("memo", "").contains("系统繁忙")) {
                        XposedHook.setIsOffline(true);
                        AntForestNotification.setContentText("系统繁忙，可能需要滑动验证");
                        Log.recordLog("系统繁忙，可能需要滑动验证");
                        return str;
                    }
                } catch (Throwable ignored) {
                }
                return str;
            } catch (Throwable t) {
                Log.i(TAG, "rpc call err:");
                Log.printStackTrace(TAG, t);
                result = null;
                if (t instanceof InvocationTargetException) {
                    String msg = t.getCause().getMessage();
                    if (!StringUtil.isEmpty(msg)) {
                        if (msg.contains("登录超时")) {
                            if (!XposedHook.getIsOffline()) {
                                XposedHook.setIsOffline(true);
                                AntForestNotification.setContentText("登录超时");
                                if (Config.timeoutRestart()) {
                                    Log.recordLog("尝试重启！");
                                    XposedHook.restartHook(Config.timeoutType(), 500, true);
                                }
                            }
                        } else if (msg.contains("[1004]") && "alipay.antmember.forest.h5.collectEnergy".equals(args0)) {
                            if (Config.waitWhenException() > 0) {
                                long waitTime = System.currentTimeMillis() + Config.waitWhenException();
                                RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
                                AntForestNotification.setContentText("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                Log.recordLog("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                            }
                            try {
                                Thread.sleep(600 + RandomUtils.delay());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            continue;
                        } else if (msg.contains("MMTPException")) {
                            result = "{\"resultCode\":\"FAIL\",\"memo\":\"MMTPException\",\"resultDesc\":\"MMTPException\"}";
                            try {
                                Thread.sleep(600 + RandomUtils.delay());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            continue;
                        }
                    }
                }
                return result;
            }
        } while (count < retryCount);
        return result;
    }

    public static Object doRequest(String args0, String args1) throws Throwable {
        try {
            Object o;
            if (rpcCallMethod.getParameterTypes().length == 12) {
                o = rpcCallMethod.invoke(
                        null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
            } else {
                o = rpcCallMethod.invoke(
                        null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1, "");
            }
            Log.i(TAG, "rpc argument: " + args0 + ", " + args1);
            return o;
        } catch (Throwable t) {
            Log.i(TAG, "rpc request [" + args0 + "] err:");
            throw t;
        }
    }

    public static String getResponse(Object resp) throws Throwable {
        String str = (String) getResponseMethod.invoke(resp);
        Log.i(TAG, "rpc response: " + str);
        return str;
    }

    public static Boolean requestTest(String args0, String args1) {
        try {
            String str = getResponse(doRequest(args0, args1));
            try {
                JSONObject jo = new JSONObject(str);
                if (jo.optString("memo", "").contains("系统繁忙")) {
                    XposedHook.setIsOffline(true);
                    AntForestNotification.setContentText("系统繁忙，可能需要滑动验证");
                    Log.recordLog("系统繁忙，可能需要滑动验证");
                    return false;
                }
            } catch (Throwable ignored) {
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "rpc check err:");
            Log.printStackTrace(TAG, t);
            if (t instanceof InvocationTargetException) {
                String msg = t.getCause().getMessage();
                if (!StringUtil.isEmpty(msg)) {
                    if (msg.contains("登录超时")) {
                        if (!XposedHook.getIsOffline()) {
                            XposedHook.setIsOffline(true);
                            AntForestNotification.setContentText("登录超时");
                            if (Config.timeoutRestart()) {
                                Log.recordLog("尝试重启！");
                                XposedHook.restartHook(Config.timeoutType(), 500, true);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String newRequest(String args0, String args1) {
        return newRequest(args0, args1, 3);
    }

    public static String newRequest(String args0, String args1, int retryCount) {
        if (XposedHook.getIsOffline()) {
            return null;
        }
        int count = 0;
        do {
            count++;
            RpcEntity rpcEntity = new RpcEntity();
            try {
                Log.i(TAG, "new rpc request: " + args0 + ", " + args1);
                newRpcCallMethod.invoke(
                        newRpcInstance, args0, false, false, "json", jsonParse.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + args0 + "\",\"requestData\":" + args1 + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, new Class[]{bridgeCallbackClazz}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) {
                                if(args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                    try {
                                        Object obj = args[0];
                                        if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                            rpcEntity.setError();
                                        }
                                        String result = (String) XposedHelpers.callMethod(obj, "toJSONString");
                                        rpcEntity.setResult(obj, result);
                                        Log.i(TAG, "new rpc response: " + result);
                                    } catch (Exception e) {
                                        Log.i(TAG, "new rpc response err:");
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
                    return rpcEntity.getResultStr();
                }
                try {
                    String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResult(), "getString", "error");
                    if ("2000".equals(errorCode)) {
                        if (!XposedHook.getIsOffline()) {
                            XposedHook.setIsOffline(true);
                            AntForestNotification.setContentText("登录超时");
                            if (Config.timeoutRestart()) {
                                Log.recordLog("尝试重启！");
                                XposedHook.restartHook(Config.timeoutType(), 500, true);
                            }
                        }
                        return null;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "new rpc response get err:");
                    Log.printStackTrace(TAG, e);
                }
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable t) {
                Log.i(TAG, "new rpc request [" + args0 + "] err:");
                Log.printStackTrace(TAG, t);
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } while (count < retryCount);
        return null;
    }

    public static String newAsyncRequest(String args0, String args1, int retryCount) {
        if (XposedHook.getIsOffline()) {
            return null;
        }
        int count = 0;
        do {
            count++;
            RpcEntity rpcEntity = new RpcEntity(Thread.currentThread());
            Long id = rpcEntity.getId();
            try {
                synchronized (id) {
                    Log.i(TAG, "new rpc request: " + args0 + ", " + args1);
                    newRpcCallMethod.invoke(
                            newRpcInstance, args0, false, false, "json", jsonParse.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + args0 + "\",\"requestData\":" + args1 + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, new Class[]{bridgeCallbackClazz}, new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) {
                                    if(args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                        try {
                                            synchronized (id) {
                                                Object obj = args[0];
                                                if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                                    rpcEntity.setError();
                                                }
                                                String result = (String) XposedHelpers.callMethod(obj, "toJSONString");
                                                rpcEntity.setResult(obj, result);
                                                Log.i(TAG, "new rpc response: " + result);
                                                Thread thread = rpcEntity.getThread();
                                                if (thread != null) {
                                                    id.notifyAll();
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.i(TAG, "new rpc response err:");
                                            Log.printStackTrace(TAG, e);
                                            synchronized (id) {
                                                Thread thread = rpcEntity.getThread();
                                                if (thread != null) {
                                                    id.notifyAll();
                                                }
                                            }
                                        }
                                    }
                                    return null;
                                }
                            })
                    );
                    id.wait(30_000);
                }
                if (!rpcEntity.getHasResult()) {
                    return null;
                }
                if (!rpcEntity.getHasError()) {
                    return rpcEntity.getResultStr();
                }
                try {
                    String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResult(), "getString", "error");
                    if ("2000".equals(errorCode)) {
                        if (!XposedHook.getIsOffline()) {
                            XposedHook.setIsOffline(true);
                            AntForestNotification.setContentText("登录超时");
                            if (Config.timeoutRestart()) {
                                Log.recordLog("尝试重启！");
                                XposedHook.restartHook(Config.timeoutType(), 500, true);
                            }
                        }
                        return null;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "new rpc response get err:");
                    Log.printStackTrace(TAG, e);
                }
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable t) {
                Log.i(TAG, "new rpc request [" + args0 + "] err:");
                Log.printStackTrace(TAG, t);
                try {
                    Thread.sleep(600 + RandomUtils.delay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                rpcEntity.delThread();
            }
        } while (count < retryCount);
        return null;
    }

    public static Boolean newRequestTest(String args0, String args1) {
        RpcEntity rpcEntity = new RpcEntity();
        try {
            Log.i(TAG, "new rpc request: " + args0 + ", " + args1);
            newRpcCallMethod.invoke(
                    newRpcInstance, args0, false, false, "json", jsonParse.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + args0 + "\",\"requestData\":" + args1 + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, new Class[]{bridgeCallbackClazz}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if(args.length == 1 && "sendJSONResponse".equals(method.getName())) {
                                try {
                                    Object obj = args[0];
                                    if (!(Boolean) XposedHelpers.callMethod(obj, "containsKey", "success")) {
                                        rpcEntity.setError();
                                    }
                                    String result = (String) XposedHelpers.callMethod(obj, "toJSONString");
                                    rpcEntity.setResult(obj, result);
                                    Log.i(TAG, "new rpc response: " + result);
                                } catch (Exception e) {
                                    Log.i(TAG, "new rpc response err:");
                                    Log.printStackTrace(TAG, e);
                                }
                            }
                            return null;
                        }
                    })
            );
            if (!rpcEntity.getHasResult()) {
                return false;
            }
            if (!rpcEntity.getHasError()) {
                return true;
            }
            try {
                String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResult(), "getString", "error");
                if ("2000".equals(errorCode)) {
                    if (!XposedHook.getIsOffline()) {
                        XposedHook.setIsOffline(true);
                        AntForestNotification.setContentText("登录超时");
                        if (Config.timeoutRestart()) {
                            Log.recordLog("尝试重启！");
                            XposedHook.restartHook(Config.timeoutType(), 500, true);
                        }
                    }
                } else {
                    if (!XposedHook.getIsOffline()) {
                        XposedHook.setIsOffline(true);
                        AntForestNotification.setContentText("其他异常");
                    }
                    if (Config.timeoutRestart()) {
                        Log.recordLog("尝试重启！");
                        XposedHook.restartHook(Config.timeoutType(), 500, true);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "new rpc response err:");
                Log.printStackTrace(TAG, e);
            }
            return false;
        } catch (Throwable t) {
            Log.i(TAG, "new rpc request [" + args0 + "] err:");
            Log.printStackTrace(TAG, t);
            if (!XposedHook.getIsOffline()) {
                XposedHook.setIsOffline(true);
                AntForestNotification.setContentText("登录超时");
                if (Config.timeoutRestart()) {
                    Log.recordLog("尝试重启！");
                    XposedHook.restartHook(Config.timeoutType(), 500, true);
                }
            }
        }
        return false;
    }

}
