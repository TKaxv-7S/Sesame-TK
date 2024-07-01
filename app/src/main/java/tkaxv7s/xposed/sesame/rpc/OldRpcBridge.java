package tkaxv7s.xposed.sesame.rpc;

import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.RuntimeInfo;
import tkaxv7s.xposed.sesame.entity.RpcEntity;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;

public class OldRpcBridge implements RpcBridge {

    private static final String TAG = OldRpcBridge.class.getSimpleName();

    private ClassLoader loader;

    private Class<?> h5PageClazz;

    private Method rpcCallMethod;

    private Method getResponseMethod;

    private Object curH5PageImpl;


    public void load() throws Exception {
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
                Log.i(TAG, "get oldRpcCallMethod successfully");
            } catch (Exception e) {
                Log.i(TAG, "get oldRpcCallMethod err:");
                throw e;
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
        int count = 0;
        do {
            count++;
            Object resp;
            try {
                if (rpcCallMethod.getParameterTypes().length == 12) {
                    resp = rpcCallMethod.invoke(
                            null, method, data, "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
                } else {
                    resp = rpcCallMethod.invoke(
                            null, method, data, "", true, null, null, false, curH5PageImpl, 0, "", false, -1, "");
                }
                Log.i(TAG, "old rpc request | id: " + id + " | method: " + method + " | data: " + data);
            } catch (Throwable t) {
                Log.i(TAG, "old rpc request | id: " + id + " | method: " + method + " err:");
                Log.printStackTrace(TAG, t);
                if (t instanceof InvocationTargetException) {
                    String msg = t.getCause().getMessage();
                    if (!StringUtil.isEmpty(msg)) {
                        if (msg.contains("登录超时")) {
                            if (!ApplicationHook.isOffline()) {
                                ApplicationHook.setOffline(true);
                                NotificationUtil.updateStatusText("登录超时");
                                if (BaseModel.getTimeoutRestart().getValue()) {
                                    Log.record("尝试重新登录");
                                    ApplicationHook.reLoginByBroadcast();
                                }
                            }
                        } else if (msg.contains("[1004]") && "alipay.antmember.forest.h5.collectEnergy".equals(method)) {
                            if (BaseModel.getWaitWhenException().getValue() > 0) {
                                long waitTime = System.currentTimeMillis() + BaseModel.getWaitWhenException().getValue();
                                RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
                                NotificationUtil.updateStatusText("异常");
                                Log.record("触发异常,等待至" + DateFormat.getDateTimeInstance().format(waitTime));
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
                        } else if (msg.contains("MMTPException")) {
                            try {
                                String jsonString = "{\"resultCode\":\"FAIL\",\"memo\":\"MMTPException\",\"resultDesc\":\"MMTPException\"}";
                                rpcEntity.setResponseObject(new JSONObject(jsonString), jsonString);
                                rpcEntity.setError();
                                return rpcEntity;
                            } catch (JSONException e) {
                                Log.printStackTrace(e);
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
                            continue;
                        }
                    }
                }
                return null;
            }
            try {
                String resultStr = (String) getResponseMethod.invoke(resp);
                Log.i(TAG, "old rpc response | id: " + id + " | data: " + resultStr);
                JSONObject resultObject = new JSONObject(resultStr);
                if (resultObject.optString("memo", "").contains("系统繁忙")) {
                    ApplicationHook.setOffline(true);
                    NotificationUtil.updateStatusText("系统繁忙，可能需要滑动验证");
                    Log.record("系统繁忙，可能需要滑动验证");
                    return null;
                }
                rpcEntity.setResponseObject(resultObject, resultStr);
                return rpcEntity;
            } catch (Throwable t) {
                Log.i(TAG, "old rpc response | id: " + id + " | method: " + method + " get err:");
            }
            return null;
        } while (count < tryCount);
        return null;
    }

}
