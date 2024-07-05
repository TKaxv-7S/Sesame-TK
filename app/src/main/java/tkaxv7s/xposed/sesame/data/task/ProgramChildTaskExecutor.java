package tkaxv7s.xposed.sesame.data.task;

import android.os.Build;
import tkaxv7s.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProgramChildTaskExecutor implements ChildTaskExecutor {

    private final Map<String, ThreadPoolExecutor> childGroupThreadPoolMap = new ConcurrentHashMap<>();

    @Override
    public Boolean addChildTask(ModelTask.ChildModelTask childTask) {
        ThreadPoolExecutor groupThreadPool = getChildGroupThreadPool(childTask.getGroup());
        groupThreadPool.execute(childTask.getRealRunnable());
        return true;
    }

    @Override
    public Boolean removeChildTask(ModelTask.ChildModelTask childTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childGroupThreadPoolMap.compute(childTask.getGroup(), (keyInner, valueInner) -> {
                if (valueInner != null) {
                    valueInner.remove(childTask.getRealRunnable());
                }
                return valueInner;
            });
        } else {
            synchronized (childGroupThreadPoolMap) {
                ThreadPoolExecutor groupThreadPool = childGroupThreadPoolMap.get(childTask.getGroup());
                if (groupThreadPool != null) {
                    groupThreadPool.remove(childTask.getRealRunnable());
                }
            }
        }
        return true;
    }

    @Override
    public Boolean clearGroupChildTask(String group) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childGroupThreadPoolMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner != null) {
                    ThreadUtil.shutdownAndAwaitTermination(valueInner, 3, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            synchronized (childGroupThreadPoolMap) {
                ThreadPoolExecutor groupThreadPool = childGroupThreadPoolMap.get(group);
                if (groupThreadPool != null) {
                    ThreadUtil.shutdownAndAwaitTermination(groupThreadPool, 3, TimeUnit.SECONDS);
                    childGroupThreadPoolMap.remove(group);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean clearAllChildTask() {
        for (ThreadPoolExecutor childThreadPool : childGroupThreadPoolMap.values()) {
            ThreadUtil.shutdownAndAwaitTermination(childThreadPool, 3, TimeUnit.SECONDS);
        }
        childGroupThreadPoolMap.clear();
        return true;
    }

    private ThreadPoolExecutor getChildGroupThreadPool(String group) {
        ThreadPoolExecutor groupThreadPool = childGroupThreadPoolMap.get(group);
        if (groupThreadPool != null) {
            return groupThreadPool;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            groupThreadPool = childGroupThreadPoolMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner == null) {
                    valueInner = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
                }
                return valueInner;
            });
        } else {
            synchronized (childGroupThreadPoolMap) {
                groupThreadPool = childGroupThreadPoolMap.get(group);
                if (groupThreadPool == null) {
                    groupThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
                    childGroupThreadPoolMap.put(group, groupThreadPool);
                }
            }
        }
        return groupThreadPool;
    }

}
