package pansong291.xposed.quickenergy.entity;

import lombok.Getter;

@Getter
public class RpcEntity {

    private final Thread requestThread;

    private final String requestMethod;

    private final String requestData;

    private volatile Boolean hasResult = false;

    private volatile Boolean hasError = false;

    private volatile Object responseObject;

    private volatile String responseString;

    public RpcEntity(String requestMethod, String requestData) {
        this.requestThread = Thread.currentThread();
        this.requestMethod = requestMethod;
        this.requestData = requestData;
    }

    public RpcEntity() {
        this(null, null);
    }

    public void setResponseObject(Object result, String resultStr) {
        this.hasResult = true;
        this.responseObject = result;
        this.responseString = resultStr;
    }

    public void setError() {
        this.hasError = true;
    }
}
