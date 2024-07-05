package tkaxv7s.xposed.sesame.data.task;

public interface ChildTaskExecutor {

    Boolean addChildTask(ModelTask.ChildModelTask childTask);

    Boolean removeChildTask(ModelTask.ChildModelTask childTask);

    Boolean clearGroupChildTask(String group);

    Boolean clearAllChildTask();

}