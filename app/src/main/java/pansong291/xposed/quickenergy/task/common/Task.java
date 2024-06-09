package pansong291.xposed.quickenergy.task.common;

import android.os.Build;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import pansong291.xposed.quickenergy.util.Log;

public abstract class Task {

    private static final String TAG = Task.class.getSimpleName();

    private static final Map<Class<? extends Task>, Task> taskMap = new ConcurrentHashMap<>();

    private static final Task[] taskArray = new Task[TaskOrder.getClazzSize()];

    private static final List<Task> taskList = new LinkedList<>(Arrays.asList(taskArray));

    private static final List<Task> readOnlyTaskList = Collections.unmodifiableList(taskList);

    @Getter
    private final Runnable runnable;

    @Getter
    private volatile Thread thread;

    private final Map<String, Thread> childThreadMap = new ConcurrentHashMap<>();

    public Task() {
        this.runnable = init();
        this.thread = null;
    }

    public abstract Runnable init();

    public abstract Boolean check();

    public synchronized Boolean hasChildThread(String childName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childThreadMap.compute(childName, (key, value) -> {
                if (value == null || !value.isAlive()) {
                    return null;
                }
                return value;
            });
        } else {
            Thread oldThread = childThreadMap.get(childName);
            if (oldThread == null || !oldThread.isAlive()) {
                childThreadMap.remove(childName);
            }
        }
        return childThreadMap.containsKey(childName);
    }

    public synchronized void addChildThread(String childName, Thread childThread) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childThreadMap.compute(childName, (key, value) -> {
                if (value != null) {
                    shutdownAndWait(value, -1, TimeUnit.SECONDS);
                }
                childThread.start();
                return childThread;
            });
        } else {
            Thread oldThread = childThreadMap.get(childName);
            if (oldThread != null) {
                shutdownAndWait(oldThread, -1, TimeUnit.SECONDS);
            }
            childThread.start();
            childThreadMap.put(childName, childThread);
        }
    }

    public synchronized void removeChildThread(String childName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childThreadMap.compute(childName, (key, value) -> {
                if (value != null) {
                    shutdownAndWait(value, -1, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            Thread oldThread = childThreadMap.get(childName);
            if (oldThread != null) {
                shutdownAndWait(oldThread, -1, TimeUnit.SECONDS);
            }
            childThreadMap.remove(childName);
        }
    }

    public synchronized Integer countChildThread() {
        return childThreadMap.size();
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
            return true;
        }
        return false;
    }

    public synchronized void stopTask() {
        if (thread != null && thread.isAlive()) {
            shutdownAndWait(thread, 5, TimeUnit.SECONDS);
        }
        for (Thread childThread : childThreadMap.values()) {
            if (childThread != null) {
                shutdownAndWait(childThread, -1, TimeUnit.SECONDS);
            }
        }
        thread = null;
        childThreadMap.clear();
    }

    public static Boolean hasTask(Class<? extends Task> taskClazz) {
        return taskMap.containsKey(taskClazz);
    }

    public static Task getTask(Class<? extends Task> taskClazz) {
        return taskMap.get(taskClazz);
    }

    public static List<Task> getTaskList() {
        return readOnlyTaskList;
    }

    public static void initAllTask() {
        removeAllTask();
        List<Class<Task>> taskClazzList = TaskOrder.getClazzList();
        for (int i = 0, len = taskClazzList.size(); i < len; i++) {
            Class<Task> taskClazz = taskClazzList.get(i);
            try {
                Task task = taskClazz.newInstance();
                taskArray[i] = task;
                taskMap.put(taskClazz, task);
            } catch (IllegalAccessException | InstantiationException e) {
                Log.printStackTrace(e);
            }
        }
    }

    public static void startAllTask() {
        startAllTask(false);
    }

    public static void startAllTask(Boolean force) {
        for (Task task : taskArray) {
            if (task != null) {
                if (task.startTask(force)) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.printStackTrace(e);
                    }
                }
            }
        }
    }

    public static void stopAllTask() {
        for (Task task : taskArray) {
            if (task != null) {
                task.stopTask();
            }
        }
    }

    public static synchronized void removeAllTask() {
        for (int i = 0, len = taskArray.length; i < len; i++) {
            Task task = taskArray[i];
            if (task != null) {
                task.stopTask();
                taskArray[i] = null;
                taskMap.remove(task.getClass());
            }
        }
    }

    public static void shutdownAndWait(Thread thread, long timeout, TimeUnit unit) {
        if (thread != null) {
            thread.interrupt();
            if (timeout > -1L) {
                try {
                    thread.join(unit.toMillis(timeout));
                } catch (InterruptedException e) {
                    Log.i(TAG, "task shutdown err:");
                    Log.printStackTrace(TAG, e);
                }
            }
        }
    }

}
