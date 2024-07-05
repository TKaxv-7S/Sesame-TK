package tkaxv7s.xposed.sesame.data.task;

import android.os.Build;
import lombok.Getter;
import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelType;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ModelTask extends Model {

    private static final Map<ModelTask, Thread> MAIN_TASK_MAP = new ConcurrentHashMap<>();

    private static final ThreadPoolExecutor MAIN_THREAD_POOL = new ThreadPoolExecutor(getModelArray().length, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

    private final Map<String, ChildModelTask> childTaskMap = new ConcurrentHashMap<>();

    private final ChildTaskExecutor childTaskExecutor;

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

    public ModelTask() {
        this.childTaskExecutor = newTimedTaskExecutor();
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

    public Boolean isSync() {
        return false;
    }

    public abstract void run();

    public Boolean hasChildTask(String childId) {
        return childTaskMap.containsKey(childId);
    }

    public ChildModelTask getChildTask(String childId) {
        return childTaskMap.get(childId);
    }

    public Boolean addChildTask(ChildModelTask childTask) {
        String childId = childTask.getId();
        if (hasChildTask(childId)) {
            removeChildTask(childId);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return childTask == childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    return value;
                }
                setRealRunnable(childTask);
                if (childTaskExecutor.addChildTask(childTask)) {
                    return childTask;
                }
                return null;
            });
        } else {
            synchronized (childTaskMap) {
                ChildModelTask oldTask = childTaskMap.get(childId);
                if (oldTask != null) {
                    return false;
                }
                setRealRunnable(childTask);
                if (childTaskExecutor.addChildTask(childTask)) {
                    childTaskMap.put(childId, childTask);
                    return true;
                }
                return false;
            }
        }
    }

    public void removeChildTask(String childId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    childTaskExecutor.removeChildTask(value);
                }
                return null;
            });
        } else {
            synchronized (childTaskMap) {
                ChildModelTask childTask = childTaskMap.get(childId);
                if (childTask != null) {
                    childTaskExecutor.removeChildTask(childTask);
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
            if (isEnable() && check()) {
                if (isSync()) {
                    mainRunnable.run();
                } else {
                    MAIN_THREAD_POOL.execute(mainRunnable);
                }
                return true;
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return false;
    }

    public synchronized void stopTask() {
        childTaskExecutor.clearAllChildTask();
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
                try {
                    if (ModelType.TASK == model.getType()) {
                        ((ModelTask) model).stopTask();
                    }
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
            }
        }
    }

    private void setRealRunnable(ModelTask.ChildModelTask childTask) {
        if (childTask.getExecTime() > 0) {
            childTask.realRunnable = () -> {
                //String modelTaskId = getName();
                //Log.i("任务模块:" + modelTaskId + " 添加子任务:" + id);
                try {
                    long delay = childTask.getExecTime() - System.currentTimeMillis();
                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (Exception e) {
                            //Log.record("任务模块:" + modelTaskId + " 中断子任务:" + id);
                            return;
                        }
                    }
                    childTask.run();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                    //Log.record("任务模块:" + modelTaskId + " 异常子任务:" + id);
                } finally {
                    removeChildTask(childTask.getId());
                    //Log.i("任务模块:" + modelTaskId + " 移除子任务:" + id);
                }
            };
        } else {
            childTask.realRunnable = () -> {
                //Log.i("任务模块:" + modelTaskId + " 添加子任务:" + id);
                try {
                    childTask.run();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                    //Log.record("任务模块:" + getName() + " 异常子任务:" + childTask.getId());
                } finally {
                    removeChildTask(childTask.getId());
                    //Log.i("任务模块:" + modelTaskId + " 移除子任务:" + id);
                }
            };
        };
    }

    private ChildTaskExecutor newTimedTaskExecutor() {
        ChildTaskExecutor childTaskExecutor;
        Integer timedTaskModel = BaseModel.getTimedTaskModel().getValue();
        if (timedTaskModel == BaseModel.TimedTaskModel.SYSTEM) {
            childTaskExecutor = new SystemChildTaskExecutor();
        } else if (timedTaskModel == BaseModel.TimedTaskModel.PROGRAM) {
            childTaskExecutor = new ProgramChildTaskExecutor();
        } else {
            throw new RuntimeException("not found childTaskExecutor");
        }
        return childTaskExecutor;
    }

    @Getter
    public class ChildModelTask implements Runnable {

        private final String id;

        private final String group;

        private final Runnable runnable;

        private final long execTime;

        private Runnable realRunnable;

        public ChildModelTask() {
            this(null, null, () -> {
            }, 0);
        }

        public ChildModelTask(String id) {
            this(id, null, () -> {
            }, 0);
        }

        public ChildModelTask(String id, String group) {
            this(id, group, () -> {
            }, 0);
        }

        protected ChildModelTask(String id, long execTime) {
            this(id, null, null, execTime);
        }

        protected ChildModelTask(String id, String group, long execTime) {
            this(id, group, null, execTime);
        }

        public ChildModelTask(String id, Runnable runnable) {
            this(id, null, runnable, 0);
        }

        public ChildModelTask(String id, String group, Runnable runnable) {
            this(id, group, runnable, 0);
        }

        public ChildModelTask(String id, String group, Runnable runnable, long execTime) {
            if (StringUtil.isEmpty(id)) {
                id = toString();
            }
            if (StringUtil.isEmpty(group)) {
                group = "DEFAULT";
            }
            if (runnable == null) {
                runnable = setRunnable();
            }
            this.id = id;
            this.group = group;
            this.runnable = runnable;
            this.execTime = execTime;
        }

        public Runnable setRunnable() {
            return null;
        }

        public final void run() {
            runnable.run();
        }
    }
}
