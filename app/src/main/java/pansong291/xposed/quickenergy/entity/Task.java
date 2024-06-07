package pansong291.xposed.quickenergy.entity;

import android.os.Build;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.Getter;
import pansong291.xposed.quickenergy.util.Log;

public class Task {

    private static final String TAG = Task.class.getSimpleName();

    private static final Map<String, Task> taskMap = new ConcurrentHashMap<>();

    @Getter
    private final String name;

    @Getter
    private final Runnable runnable;

    private final Supplier<Boolean> check;

    @Getter
    private volatile Thread thread;

    private final Map<String, Thread> childThreadMap = new ConcurrentHashMap<>();

    public Task(String name, Runnable runnable, Supplier<Boolean> check) {
        this.name = name;
        this.runnable = runnable;
        this.check = check;
        this.thread = null;
    }

    public void startTask() {
        startTask(false);
    }

    public synchronized void startTask(Boolean force) {
        if (thread != null && thread.isAlive()) {
            if (!force) {
                return;
            }
            stopTask();
        }
        thread = new Thread(runnable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (check.get()) {
                thread.start();
            }
        } else {
            thread.start();
        }
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


    public static Task getTask(String name) {
        return taskMap.get(name);
    }

    public static Boolean hasTask(String name) {
        return taskMap.containsKey(name);
    }

    public static void putTask(Task task) {
        String name = task.getName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskMap.compute(name, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                return task;
            });
        } else {
            Task oldTask = taskMap.get(name);
            if (oldTask != null) {
                oldTask.stopTask();
            }
            taskMap.put(name, task);
        }
    }

    public static void putAndStartTask(Task task) {
        putTask(task);
        task.startTask();
    }

    public static void removeTask(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskMap.compute(name, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                return null;
            });
        } else {
            Task oldTask = taskMap.get(name);
            if (oldTask != null) {
                oldTask.stopTask();
            }
            taskMap.remove(name);
        }
    }

    public static void startTask(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskMap.compute(name, (key, value) -> {
                if (value != null) {
                    value.startTask(false);
                }
                return value;
            });
        } else {
            Task task = taskMap.get(name);
            if (task != null) {
                task.startTask(false);
            }
        }
    }

    public static void startTask(String name, Boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskMap.compute(name, (key, value) -> {
                if (value != null) {
                    value.startTask(force);
                }
                return value;
            });
        } else {
            Task task = taskMap.get(name);
            if (task != null) {
                task.startTask(force);
            }
        }
    }

    public static void stopTask(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskMap.compute(name, (key, value) -> {
                if (value != null) {
                    value.stopTask();
                }
                return value;
            });
        } else {
            Task task = taskMap.get(name);
            if (task != null) {
                task.stopTask();
            }
        }
    }

    public static void startAllTask() {
        startAllTask(false);
    }

    public static void startAllTask(Boolean force) {
        for (Task task : taskMap.values()) {
            task.startTask(force);
        }
    }

    public static void stopAllTask() {
        for (Task task : taskMap.values()) {
            task.stopTask();
        }
    }

    public static void removeAllTask() {
        Iterator<Task> iterator = taskMap.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().stopTask();
            iterator.remove();
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
