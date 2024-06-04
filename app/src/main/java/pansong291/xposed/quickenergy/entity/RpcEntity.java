package pansong291.xposed.quickenergy.entity;

public class RpcEntity {

    private final Long id;

    private volatile Thread thread;

    private volatile Boolean hasResult = false;

    private volatile String result;

    public Long getId() {
        return id;
    }

    public Thread getThread() {
        return thread;
    }

    public Boolean getHasResult() {
        return hasResult;
    }

    public String getResult() {
        return result;
    }

    public RpcEntity(Thread thread) {
        this.id = thread.getId();
        this.thread = thread;
        this.result = null;
    }

    public void delThread() {
        this.thread = null;
    }

    public void setResult(String result) {
        this.hasResult = true;
        this.result = result;
    }
}
