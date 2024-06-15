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

    public abstract Runnable init();

    public abstract Boolean check();

    public synchronized Boolean hasChildTask(String childName) {
        return childTaskMap.containsKey(childName);
    }

    public synchronized BaseTask getChildTask(String childName) {
        return childTaskMap.get(childName);
    }

    public synchronized void addChildTask(String childName, BaseTask childTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childName, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                childTask.startTask();
                return childTask;
            });
        } else {
            BaseTask oldTask = childTaskMap.get(childName);
            if (oldTask != null) {
                oldTask.stopTask();
            }
            childTask.startTask();
            childTaskMap.put(childName, childTask);
        }
    }

    public synchronized void removeChildTask(String childName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childName, (key, value) -> {
                if (value != null) {
                    ThreadUtil.shutdownAndWait(value.getThread(), -1, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            BaseTask oldTask = childTaskMap.get(childName);
            if (oldTask != null) {
                ThreadUtil.shutdownAndWait(oldTask.getThread(), -1, TimeUnit.SECONDS);
            }
            childTaskMap.remove(childName);
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

}
