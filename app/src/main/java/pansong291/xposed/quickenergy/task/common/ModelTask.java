package pansong291.xposed.quickenergy.task.common;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pansong291.xposed.quickenergy.data.ModelConfig;
import pansong291.xposed.quickenergy.data.ModelFields;
import pansong291.xposed.quickenergy.util.Log;

public abstract class ModelTask extends BaseTask {

    @SuppressLint("StaticFieldLeak")
    protected static Context context;

    private static final Map<String, ModelConfig> modelConfigMap = new LinkedHashMap<>();

    private static final Map<String, ModelConfig> readOnlyModelConfigMap = Collections.unmodifiableMap(modelConfigMap);

    private static final Map<Class<? extends ModelTask>, ModelTask> taskMap = new ConcurrentHashMap<>();

    private static final ModelTask[] taskArray = new ModelTask[TaskOrder.getClazzSize()];

    private static final List<ModelTask> taskList = new LinkedList<>(Arrays.asList(taskArray));

    private static final List<ModelTask> readOnlyTaskList = Collections.unmodifiableList(taskList);

    public abstract String setName();

    public abstract ModelFields setFields();

    public static Map<String, ModelConfig> getModelConfigMap() {
        return readOnlyModelConfigMap;
    }

    public static Boolean hasTask(Class<? extends ModelTask> taskClazz) {
        return taskMap.containsKey(taskClazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ModelTask> T getTask(Class<T> taskClazz) {
        return (T) taskMap.get(taskClazz);
    }

    public static List<ModelTask> getTaskList() {
        return readOnlyTaskList;
    }

    public static void initAllTask(Context context) {
        ModelTask.context = context;
        removeAllTask();
        List<Class<ModelTask>> taskClazzList = TaskOrder.getClazzList();
        for (int i = 0, len = taskClazzList.size(); i < len; i++) {
            Class<ModelTask> taskClazz = taskClazzList.get(i);
            try {
                ModelTask task = taskClazz.newInstance();
                ModelConfig modelConfig = new ModelConfig(task);
                modelConfigMap.put(modelConfig.getCode(), modelConfig);
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
        for (ModelTask task : taskArray) {
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
        for (ModelTask task : taskArray) {
            if (task != null) {
                task.stopTask();
            }
        }
    }

    public static synchronized void removeAllTask() {
        for (int i = 0, len = taskArray.length; i < len; i++) {
            ModelTask task = taskArray[i];
            if (task != null) {
                task.stopTask();
                try {
                    task.destroy();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
                taskArray[i] = null;
                taskMap.remove(task.getClass());
            }
        }
    }

}
