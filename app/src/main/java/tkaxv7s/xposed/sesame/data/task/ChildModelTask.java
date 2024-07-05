//package tkaxv7s.xposed.sesame.data.task;
//
//import lombok.Getter;
//import lombok.Setter;
//import tkaxv7s.xposed.sesame.util.StringUtil;
//
//@Getter
//public class ChildModelTask implements Runnable {
//
//    private final String id;
//
//    private final String group;
//
//    private final Runnable runnable;
//
//    private final long execTime;
//
//    @Setter
//    private Runnable realRunnable;
//
//    public ChildModelTask() {
//        this(null, null, () -> {
//        }, 0);
//    }
//
//    public ChildModelTask(String id) {
//        this(id, null, () -> {
//        }, 0);
//    }
//
//    public ChildModelTask(String id, String group) {
//        this(id, group, () -> {
//        }, 0);
//    }
//
//    protected ChildModelTask(String id, long execTime) {
//        this(id, null, null, execTime);
//    }
//
//    protected ChildModelTask(String id, String group, long execTime) {
//        this(id, group, null, execTime);
//    }
//
//    public ChildModelTask(String id, Runnable runnable) {
//        this(id, null, runnable, 0);
//    }
//
//    public ChildModelTask(String id, String group, Runnable runnable) {
//        this(id, group, runnable, 0);
//    }
//
//    public ChildModelTask(String id, String group, Runnable runnable, long execTime) {
//        if (StringUtil.isEmpty(id)) {
//            id = toString();
//        }
//        if (StringUtil.isEmpty(group)) {
//            group = "DEFAULT";
//        }
//        if (runnable == null) {
//            runnable = setRunnable();
//        }
//        this.id = id;
//        this.group = group;
//        this.runnable = runnable;
//        this.execTime = execTime;
//    }
//
//    public Runnable setRunnable() {
//        return null;
//    }
//
//    public final void run() {
//        runnable.run();
//    }
//}