package pansong291.xposed.quickenergy.task.common;

import android.os.Build;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import pansong291.xposed.quickenergy.util.ThreadUtil;

public abstract class BaseTask {

    @Getter
    private final Runnable runnable;

    @Getter
    private volatile Thread thread;

    private final Map<String, BaseTask> childTaskMap = new ConcurrentHashMap<>();

    public BaseTask() {
        this.runnable = init();
        this.thread = null;
    }

    public String getId() {
        return toString();
    }

    public abstract Boolean check();

    public abstract Runnable init();

    public void destroy() {

    }

    public synchronized Boolean hasChildTask(String childId) {
        return childTaskMap.containsKey(childId);
    }

    public synchronized BaseTask getChildTask(String childId) {
        return childTaskMap.get(childId);
    }

    public synchronized void addChildTask(BaseTask childTask) {
        String childId = childTask.getId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                childTask.startTask();
                return childTask;
            });
        } else {
            BaseTask oldTask = childTaskMap.get(childId);
            if (oldTask != null) {
                oldTask.stopTask();
            }
            childTask.startTask();
            childTaskMap.put(childId, childTask);
        }
    }

    public synchronized void removeChildTask(String childId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    ThreadUtil.shutdownAndWait(value.getThread(), -1, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            BaseTask oldTask = childTaskMap.get(childId);
            if (oldTask != null) {
                ThreadUtil.shutdownAndWait(oldTask.getThread(), -1, TimeUnit.SECONDS);
            }
            childTaskMap.remove(childId);
        }
    }

    public synchronized Integer countChildTask() {
        return childTaskMap.size();
    }

    public Boolean startTask() {
        return startTask(false);
    }

    public synchronized Boolean startTask(Boolean force) {
        if (thread != null && thread.isAlive()) {
            if (!force) {
                return false;
            }
            stopTask();
        }
        thread = new Thread(runnable);
        if (check()) {
            thread.start();
            for (BaseTask childTask : childTaskMap.values()) {
                if (childTask != null) {
                    childTask.startTask();
                }
            }
            return true;
        }
        return false;
    }

    public synchronized void stopTask() {
        if (thread != null && thread.isAlive()) {
            ThreadUtil.shutdownAndWait(thread, 5, TimeUnit.SECONDS);
        }
        for (BaseTask childTask : childTaskMap.values()) {
            if (childTask != null) {
                ThreadUtil.shutdownAndWait(childTask.getThread(), -1, TimeUnit.SECONDS);
            }
        }
        thread = null;
    }

    public static BaseTask newInstance() {
        return new BaseTask() {
            @Override
            public Runnable init() {
                return () -> {};
            }

            @Override
            public Boolean check() {
                return true;
            }
        };
    }

    public static BaseTask newInstance(String id) {
        return new BaseTask() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Runnable init() {
                return () -> {};
            }

            @Override
            public Boolean check() {
                return true;
            }
        };
    }

}
