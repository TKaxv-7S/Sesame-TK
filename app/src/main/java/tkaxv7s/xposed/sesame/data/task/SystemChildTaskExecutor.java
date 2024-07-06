package tkaxv7s.xposed.sesame.data.task;

import android.os.Build;
import android.os.Handler;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.*;

public class SystemChildTaskExecutor implements ChildTaskExecutor {

    private final Handler handler;

    private final Map<String, ThreadPoolExecutor> groupChildTaskExecutorMap = new ConcurrentHashMap<>();

    public SystemChildTaskExecutor() {
        handler = ApplicationHook.getMainHandler();
    }

    @Override
    public Boolean addChildTask(ModelTask.ChildModelTask childTask) {
        ThreadPoolExecutor threadPoolExecutor = getChildGroupHandler(childTask.getGroup());
        long execTime = childTask.getExecTime();
        if (execTime > 0) {
            Runnable runnable = () -> {
                if (childTask.getIsCancel()) {
                    return;
                }
                //String modelTaskId = getName();
                //Log.i("任务模块:" + modelTaskId + " 添加子任务:" + id);
                Future<?> future = threadPoolExecutor.submit(() -> {
                    try {
                        childTask.run();
                    } catch (Exception e) {
                        Log.printStackTrace(e);
                        //Log.record("任务模块:" + modelTaskId + " 异常子任务:" + id);
                    } finally {
                        childTask.getModelTask().removeChildTask(childTask.getId());
                        //Log.i("任务模块:" + modelTaskId + " 移除子任务:" + id);
                    }
                });
                childTask.setCancelTask(() -> future.cancel(true));
            };
            childTask.setCancelTask(() -> handler.removeCallbacks(runnable));
            handler.postDelayed(runnable, execTime - System.currentTimeMillis());
        } else {
            Future<?> future = threadPoolExecutor.submit(() -> {
                //Log.i("任务模块:" + modelTaskId + " 添加子任务:" + id);
                try {
                    childTask.run();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                    //Log.record("任务模块:" + getName() + " 异常子任务:" + childTask.getId());
                } finally {
                    childTask.getModelTask().removeChildTask(childTask.getId());
                    //Log.i("任务模块:" + modelTaskId + " 移除子任务:" + id);
                }
            });
            childTask.setCancelTask(() -> future.cancel(true));
        }
        return true;
    }

    @Override
    public Boolean removeChildTask(ModelTask.ChildModelTask childTask) {
        childTask.cancel();
        return true;
    }

    @Override
    public Boolean clearGroupChildTask(String group) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            groupChildTaskExecutorMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner != null) {
                    ThreadUtil.shutdownAndAwaitTermination(valueInner, 3, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            synchronized (groupChildTaskExecutorMap) {
                ThreadPoolExecutor threadPoolExecutor = groupChildTaskExecutorMap.get(group);
                if (threadPoolExecutor != null) {
                    ThreadUtil.shutdownAndAwaitTermination(threadPoolExecutor, 3, TimeUnit.SECONDS);
                    groupChildTaskExecutorMap.remove(group);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean clearAllChildTask() {
        for (ThreadPoolExecutor threadPoolExecutor : groupChildTaskExecutorMap.values()) {
            ThreadUtil.shutdownAndAwaitTermination(threadPoolExecutor, 3, TimeUnit.SECONDS);
        }
        groupChildTaskExecutorMap.clear();
        return true;
    }

    private ThreadPoolExecutor getChildGroupHandler(String group) {
        ThreadPoolExecutor threadPoolExecutor = groupChildTaskExecutorMap.get(group);
        if (threadPoolExecutor != null) {
            return threadPoolExecutor;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            threadPoolExecutor = groupChildTaskExecutorMap.compute(group, (keyInner, valueInner) -> {
                if (valueInner == null) {
                    valueInner = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
                }
                return valueInner;
            });
        } else {
            synchronized (groupChildTaskExecutorMap) {
                threadPoolExecutor = groupChildTaskExecutorMap.get(group);
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
                    groupChildTaskExecutorMap.put(group, threadPoolExecutor);
                }
            }
        }
        return threadPoolExecutor;
    }

}
