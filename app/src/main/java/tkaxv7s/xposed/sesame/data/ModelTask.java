package tkaxv7s.xposed.sesame.data;

import android.os.Build;
import lombok.Getter;
import tkaxv7s.xposed.sesame.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ModelTask extends Model {

    private static final Map<ModelTask, Thread> MAIN_TASK_MAP = new ConcurrentHashMap<>();

    private static final ThreadPoolExecutor MAIN_THREAD_POOL = new ThreadPoolExecutor(getModelArray().length, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

    private final ThreadPoolExecutor childThreadPool;

    @Getter
    private final Runnable mainRunnable = new Runnable() {

        private final ModelTask task = ModelTask.this;

        @Override
        public void run() {
            if (MAIN_TASK_MAP.get(task) != null) {
                return;
            }
            MAIN_TASK_MAP.put(task, Thread.currentThread());
            try {
                task.run();
            } catch (Exception e) {
                Log.printStackTrace(e);
            } finally {
                MAIN_TASK_MAP.remove(task);
            }
        }

    };

    private final Map<String, ChildModelTask> childTaskMap = new ConcurrentHashMap<>();

    public ModelTask() {
        this(0);
    }

    public ModelTask(int childThreadSize) {
        childThreadPool = new ThreadPoolExecutor(childThreadSize, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public String getId() {
        return toString();
    }

    public ModelType getType() {
        return ModelType.TASK;
    }

    public abstract String getName();

    public abstract ModelFields getFields();

    public abstract Boolean check();

    public abstract void run();

    public Boolean hasChildTask(String childId) {
        return childTaskMap.containsKey(childId);
    }

    public ChildModelTask getChildTask(String childId) {
        return childTaskMap.get(childId);
    }

    public void addChildTask(ChildModelTask childTask) {
        String childId = childTask.getId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    childThreadPool.remove(value);
                }
                return null;
            });
        } else {
            synchronized (childTaskMap) {
                ChildModelTask oldTask = childTaskMap.remove(childId);
                if (oldTask != null) {
                    childThreadPool.remove(oldTask);
                }
            }
        }
        childThreadPool.execute(childTask);
    }

    public void removeChildTask(String childId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    childThreadPool.remove(value);
                }
                return null;
            });
        } else {
            synchronized (childTaskMap) {
                ChildModelTask oldTask = childTaskMap.get(childId);
                if (oldTask != null) {
                    childThreadPool.remove(oldTask);
                }
                childTaskMap.remove(childId);
            }
        }
    }

    public Integer countChildTask() {
        return childTaskMap.size();
    }

    public Boolean startTask() {
        return startTask(false);
    }

    public synchronized Boolean startTask(Boolean force) {
        if (MAIN_TASK_MAP.containsKey(this)) {
            if (!force) {
                return false;
            }
            stopTask();
        }
        try {
            if (check()) {
                MAIN_THREAD_POOL.execute(mainRunnable);
                return true;
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return false;
    }

    public synchronized void stopTask() {
        childThreadPool.purge();
        childTaskMap.clear();
        MAIN_THREAD_POOL.remove(mainRunnable);
        MAIN_TASK_MAP.remove(this);
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

    @Getter
    public static class ChildModelTask implements Runnable {

        private final ModelTask modelTask;

        private final String id;

        private final Runnable runnable;

        private final long execTime;

        public ChildModelTask(ModelTask modelTask) {
            this(modelTask, null, () -> {}, 0);
        }

        public ChildModelTask(ModelTask modelTask, String id) {
            this(modelTask, id, () -> {}, 0);
        }

        public ChildModelTask(ModelTask modelTask, String id, int execTime) {
            this(modelTask, id, null, execTime);
        }

        public ChildModelTask(ModelTask modelTask, String id, Runnable runnable) {
            this(modelTask, id, runnable, 0);
        }

        public ChildModelTask(ModelTask modelTask, String id, Runnable runnable, int execTime) {
            if (id == null) {
                id = toString();
            }
            if (runnable == null) {
                runnable = setRunnable();
            }
            this.modelTask = modelTask;
            this.id = id;
            this.runnable = runnable;
            this.execTime = execTime;
        }

        public Runnable setRunnable() {
            return null;
        }

        public final void run() {
            Map<String, ChildModelTask> childTaskMap = modelTask.childTaskMap;
            if (childTaskMap.get(id) != null) {
                return;
            }
            String modelTaskId = modelTask.getName();
            childTaskMap.put(id, this);
            //Log.record("模块:" + modelTaskId + " 添加子任务:" + id);
            try {
                long delay = execTime - System.currentTimeMillis();
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Log.record("模块:" + modelTaskId + " 中断子任务:" + id);
                        return;
                    }
                }
                runnable.run();
            } catch (Exception e) {
                Log.printStackTrace(e);
                Log.record("模块:" + modelTaskId + " 异常子任务:" + id);
            } finally {
                childTaskMap.remove(id);
                //Log.record("模块:" + modelTaskId + " 移除子任务:" + id);
            }
        }
    }
}
