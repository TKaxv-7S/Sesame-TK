package tkaxv7s.xposed.sesame.data.task;

import android.os.Build;
import android.os.Handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemChildTaskExecutor implements ChildTaskExecutor {

    private final Map<String, Handler> childGroupHandlerMap = new ConcurrentHashMap<>();

    @Override
    public Boolean addChildTask(ModelTask.ChildModelTask childTask) {
        Handler handler = getChildGroupHandler(childTask.getGroup());
        handler.postAtTime(childTask.getRealRunnable(), childTask.getExecTime());
        return true;
    }

    @Override
    public Boolean removeChildTask(ModelTask.ChildModelTask childTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childGroupHandlerMap.compute(childTask.getGroup(), (keyInner, valueInner) -> {
                if (valueInner != null) {
                    valueInner.removeCallbacks(childTask.getRealRunnable());
                }
                return valueInner;
            });
        } else {
            synchronized (childGroupHandlerMap) {
                Handler handler = childGroupHandlerMap.get(childTask.getGroup());
                if (handler != null) {
                    handler.removeCallbacks(childTask.getRealRunnable());
                }
            }
        }
        return true;
    }

    @Override
    public Boolean clearGroupChildTask(String group) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childGroupHandlerMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner != null) {
                    valueInner.removeCallbacksAndMessages(null);
                }
                return null;
            });
        } else {
            synchronized (childGroupHandlerMap) {
                Handler handler = childGroupHandlerMap.get(group);
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                    childGroupHandlerMap.remove(group);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean clearAllChildTask() {
        for (Handler childHandler : childGroupHandlerMap.values()) {
            childHandler.removeCallbacksAndMessages(null);
        }
        childGroupHandlerMap.clear();
        return true;
    }

    private Handler getChildGroupHandler(String group) {
        Handler groupThreadPool = childGroupHandlerMap.get(group);
        if (groupThreadPool != null) {
            return groupThreadPool;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            groupThreadPool = childGroupHandlerMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner == null) {
                    valueInner = new Handler();
                }
                return valueInner;
            });
        } else {
            synchronized (childGroupHandlerMap) {
                groupThreadPool = childGroupHandlerMap.get(group);
                if (groupThreadPool == null) {
                    groupThreadPool = new Handler();
                    childGroupHandlerMap.put(group, groupThreadPool);
                }
            }
        }
        return groupThreadPool;
    }

}
