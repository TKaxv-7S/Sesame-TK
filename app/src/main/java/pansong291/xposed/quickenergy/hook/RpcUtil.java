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
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;
import pansong291.xposed.quickenergy.util.StringUtil;

public class RpcUtil {

    private static final String TAG = RpcUtil.class.getCanonicalName();

    private static ClassLoader loader;


    private static Method rpcCallMethod;
    private static Method getResponseMethod;
    private static Object curH5PageImpl;



    private static Object newRpcInstance;

    private static Method newRpcCallMethod;

    private static Class<?> JSONObjectClazz;
    private static Class<?> jsonClazz;
    private static Method jsonParse;
    private static Class<?> bridgeCallbackClazz;


    public static void init(ClassLoader loader) {
        RpcUtil.loader = loader;
        try {
            JSONObjectClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
            jsonClazz = loader.loadClass("com.alibaba.fastjson.JSON");
            jsonParse = jsonClazz.getMethod("parseObject", String.class);
            bridgeCallbackClazz = loader.loadClass("com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback");
            Log.i(TAG, "loadClass successfully");
        } catch (Throwable t) {
            Log.i(TAG, "loadClass err:");
            Log.printStackTrace(TAG, t);
        }
        if (rpcCallMethod == null) {
            try {
                Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
                Class<?> rpcClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil);
                rpcCallMethod = rpcClazz.getMethod(
                        "rpcCall", String.class, String.class, String.class,
                        boolean.class, JSONObjectClazz, String.class, boolean.class, h5PageClazz,
                        int.class, String.class, boolean.class, int.class, String.class);
                Log.i(TAG, "get RpcCallMethod successfully");
            } catch (Throwable t) {
                Log.i(TAG, "get RpcCallMethod err:");
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
                        throw new RuntimeException("newRpcInstance is null");
                    }
                }
                newRpcCallMethod = newRpcInstance.getClass().getMethod("rpc"
                        , String.class
                        , boolean.class
                        , boolean.class
                        , String.class
                        , JSONObjectClazz
                        , String.class
                        , JSONObjectClazz
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
                Log.debug("newRpcInstance: " + newRpcInstance.getClass().getName());
            } catch (Exception e) {
                Log.debug("newRpcInstance err:");
                Log.debug(android.util.Log.getStackTraceString(e));
            }

        }
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
        return request(args0, args1, 3);
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

    public static String newDoRequest(String args0, String args1) {
        try {
            Thread thread = Thread.currentThread();
            final String[] result = {"0", null};
            synchronized (thread) {
                newRpcCallMethod.invoke(
                        newRpcInstance, args0, false, false, "json", jsonParse.invoke(null, "{\"__apiCallStartTime\":" + System.currentTimeMillis() + ",\"apiCallLink\":\"XRiverNotFound\",\"execEngine\":\"XRiver\",\"operationType\":\"" + args0 + "\",\"requestData\":" + args1 + "}"), "", null, true, false, 0, false, "", null, null, null, Proxy.newProxyInstance(loader, new Class[]{bridgeCallbackClazz}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) {
                                try {
                                    if(method.getName().equals("sendJSONResponse")){
                                        result[1] = (String) XposedHelpers.callMethod(args[0], "toJSONString");
                                        result[0] = "1";
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "new rpc response err:");
                                    Log.printStackTrace(TAG, e);
                                } finally {
                                    thread.notifyAll();
                                }
                                return null;
                            }
                        })
                );
                Log.i(TAG, "new rpc argument: " + args0 + ", " + args1);
                thread.wait(30_000);
            }
            if (result[0].equals("1")) {
                String res = result[1];
                Log.i(TAG, "new rpc response: " + res);
                return res;
            }
        } catch (Throwable t) {
            Log.i(TAG, "new rpc request [" + args0 + "] err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
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
        if (getResponseMethod == null) {
            getResponseMethod = resp.getClass().getMethod("getResponse");
        }
        String str = (String) getResponseMethod.invoke(resp);
        Log.i(TAG, "rpc response: " + str);
        return str;
    }

}
