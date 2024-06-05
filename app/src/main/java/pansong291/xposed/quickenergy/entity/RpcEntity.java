package pansong291.xposed.quickenergy.entity;

public class RpcEntity {

    private final Long id;

    private volatile Thread thread;

    private volatile Boolean hasResult = false;

    private volatile Boolean hasError = false;

    private volatile Object result;

    private volatile String resultStr;

    public Long getId() {
        return id;
    }

    public Thread getThread() {
        return thread;
    }

    public Boolean getHasResult() {
        return hasResult;
    }

    public Object getResult() {
        return result;
    }

    public String getResultStr() {
        return resultStr;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public RpcEntity(Thread thread) {
        this.id = thread.getId();
        this.thread = thread;
        this.result = null;
    }

    public RpcEntity() {
        this.id = null;
        this.thread = null;
        this.result = null;
    }

    public void delThread() {
        this.thread = null;
    }

    public void setResult(Object result, String resultStr) {
        this.hasResult = true;
        this.result = result;
        this.resultStr = resultStr;
    }

    public void setError() {
        this.hasError = true;
    }
}
