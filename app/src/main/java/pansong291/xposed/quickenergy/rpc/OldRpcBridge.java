package pansong291.xposed.quickenergy.rpc;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.function.Function;

import pansong291.xposed.quickenergy.data.RuntimeInfo;
import pansong291.xposed.quickenergy.hook.ApplicationHook;
import pansong291.xposed.quickenergy.model.AntForestNotification;
import pansong291.xposed.quickenergy.util.ClassUtil;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;
import pansong291.xposed.quickenergy.util.StringUtil;

public class OldRpcBridge implements RpcBridge{

    private static final String TAG = OldRpcBridge.class.getSimpleName();

    private ClassLoader loader;

    private Class<?> h5PageClazz;

    private Method rpcCallMethod;

    private Method getResponseMethod;

    private Object curH5PageImpl;


    public void load() {
        loader = ApplicationHook.getClassLoader();
        try {
            h5PageClazz = loader.loadClass(ClassUtil.H5PAGE_NAME);
            Log.i(TAG, "rpc loadClass successfully");
        } catch (ClassNotFoundException t) {
            Log.i(TAG, "rpc loadClass err:");
            Log.printStackTrace(TAG, t);
            throw new RuntimeException(t);
        } catch (Throwable t) {
            Log.i(TAG, "rpc loadClass err:");
            Log.printStackTrace(TAG, t);
            throw t;
        }
        if (rpcCallMethod == null) {
            try {
                rpcCallMethod = loader.loadClass("com.alipay.mobile.nebulaappproxy.api.rpc.H5RpcUtil").getMethod(
                        "rpcCall", String.class, String.class, String.class,
                        boolean.class, loader.loadClass(ClassUtil.JSON_OBJECT_NAME), String.class, boolean.class, h5PageClazz,
                        int.class, String.class, boolean.class, int.class, String.class);
                getResponseMethod = loader.loadClass("com.alipay.mobile.nebulaappproxy.api.rpc.H5Response").getMethod("getResponse");
                Log.i(TAG, "get rpcCallMethod successfully");
            } catch (Throwable t) {
                Log.i(TAG, "get rpcCallMethod err:");
                Log.printStackTrace(TAG, t);
            }
        }
    }

    @Override
    public void unload() {
        getResponseMethod = null;
        rpcCallMethod = null;
        h5PageClazz = null;
        loader = null;
    }

    @Override
    public <T> T requestObj(String method, String data, Function<Object, T> callback, int retryCount) {
        return null;
    }

    public String requestJson(String method, String data, int retryCount) {
        if (ApplicationHook.isOffline()) {
            return null;
        }
        String result;
        int count = 0;
        do {
            count++;
            try {
                String str = getResponse(doRequest(method, data));
                try {
                    JSONObject jo = new JSONObject(str);
                    if (jo.optString("memo", "").contains("系统繁忙")) {
                        ApplicationHook.setOffline(true);
                        AntForestNotification.setContentText("系统繁忙，可能需要滑动验证");
                        Log.record("系统繁忙，可能需要滑动验证");
                        return null;
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
                            if (!ApplicationHook.isOffline()) {
                                ApplicationHook.setOffline(true);
                                AntForestNotification.setContentText("登录超时");
                                if (Config.INSTANCE.isTimeoutRestart()) {
                                    Log.record("尝试重新登录");
                                    ApplicationHook.reLoginByBroadcast();
                                }
                            }
                        } else if (msg.contains("[1004]") && "alipay.antmember.forest.h5.collectEnergy".equals(method)) {
                            if (Config.INSTANCE.getWaitWhenException() > 0) {
                                long waitTime = System.currentTimeMillis() + Config.INSTANCE.getWaitWhenException();
                                RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
                                AntForestNotification.setContentText("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
                                Log.record("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
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

    public Object doRequest(String args0, String args1) throws Throwable {
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

    public String getResponse(Object resp) throws Throwable {
        String str = (String) getResponseMethod.invoke(resp);
        Log.i(TAG, "rpc response: " + str);
        return str;
    }

}
