package tkaxv7s.xposed.sesame.data.task;

import android.os.Build;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.*;

public class ProgramChildTaskExecutor implements ChildTaskExecutor {

    private final Map<String, ThreadPoolExecutor> groupChildTaskExecutorMap = new ConcurrentHashMap<>();

    @Override
    public Boolean addChildTask(ModelTask.ChildModelTask childTask) {
        ThreadPoolExecutor threadPoolExecutor = getChildGroupThreadPool(childTask.getGroup());
        Future<?> future;
        if (childTask.getExecTime() > 0) {
            future = threadPoolExecutor.submit(() -> {
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
                    childTask.getModelTask().removeChildTask(childTask.getId());
                    //Log.i("任务模块:" + modelTaskId + " 移除子任务:" + id);
                }
            });
        } else {
            future = threadPoolExecutor.submit(() -> {
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
        }
        childTask.setCancelTask(() -> future.cancel(true));
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
                ThreadPoolExecutor groupThreadPool = groupChildTaskExecutorMap.get(group);
                if (groupThreadPool != null) {
                    ThreadUtil.shutdownAndAwaitTermination(groupThreadPool, 3, TimeUnit.SECONDS);
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

    private ThreadPoolExecutor getChildGroupThreadPool(String group) {
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
