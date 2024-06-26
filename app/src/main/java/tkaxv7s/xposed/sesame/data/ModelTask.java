package tkaxv7s.xposed.sesame.data;

import android.os.Build;
import lombok.Getter;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class ModelTask extends Model {

    @Getter
    private final Runnable runnable;

    @Getter
    private volatile Thread thread;

    private final Map<String, BaseTask> childTaskMap = new ConcurrentHashMap<>();

    public ModelTask() {
        this.runnable = this::run;
        this.thread = null;
    }

    public String getId() {
        return toString();
    }

    public ModelType getType() {
        return ModelType.TASK;
    }

    public abstract String setName();

    public abstract ModelFields setFields();

    public abstract Boolean check();

    public abstract void run();

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
        try {
            if (check()) {
                thread.start();
                for (BaseTask childTask : childTaskMap.values()) {
                    if (childTask != null) {
                        childTask.startTask();
                    }
                }
                return true;
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
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
        childTaskMap.clear();
    }

    public static void startAllTask() {
        startAllTask(false);
    }

    public static void startAllTask(Boolean force) {
        for (Model model : getModelArray()) {
            if (model != null) {
                if (ModelType.TASK == model.getType()) {
                    if (((ModelTask) model).startTask(force)) {
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            Log.printStackTrace(e);
                        }
                    }
                }
            }
        }
    }

    public static void stopAllTask() {
        for (Model model : getModelArray()) {
            if (model != null) {
                if (ModelType.TASK == model.getType()) {
                    ((ModelTask) model).stopTask();
                }
            }
        }
    }

}
