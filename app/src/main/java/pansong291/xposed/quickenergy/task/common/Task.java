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
import pansong291.xposed.quickenergy.util.ThreadUtil;

public abstract class Task {

    private static final Map<Class<? extends Task>, Task> taskMap = new ConcurrentHashMap<>();

    private static final Task[] taskArray = new Task[TaskOrder.getClazzSize()];

    private static final List<Task> taskList = new LinkedList<>(Arrays.asList(taskArray));

    private static final List<Task> readOnlyTaskList = Collections.unmodifiableList(taskList);

    @Getter
    private final Runnable runnable;

    @Getter
    private volatile Thread thread;

    private final Map<String, Task> childTaskMap = new ConcurrentHashMap<>();

    public Task() {
        this.runnable = init();
        this.thread = null;
    }

    public abstract Runnable init();

    public abstract Boolean check();

    public synchronized Boolean hasChildTask(String childName) {
        return childTaskMap.containsKey(childName);
    }

    public synchronized void addChildTask(String childName, Task childTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childName, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                childTask.startTask();
                return childTask;
            });
        } else {
            Task oldTask = childTaskMap.get(childName);
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
            Task oldTask = childTaskMap.get(childName);
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
            for (Task childTask : childTaskMap.values()) {
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
        for (Task childTask : childTaskMap.values()) {
            if (childTask != null) {
                ThreadUtil.shutdownAndWait(childTask.getThread(), -1, TimeUnit.SECONDS);
            }
        }
        thread = null;
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
                        Thread.sleep(80);
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

}
