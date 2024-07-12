package tkaxv7s.xposed.sesame.entity;

import lombok.Getter;

@Getter
public class RpcEntity {

    private final Thread requestThread;

    private final String requestMethod;

    private final String requestData;

    private final String requestRelation;

    private final Long requestTime;

    private volatile Boolean hasResult = false;

    private volatile Boolean hasError = false;

    private volatile Object responseObject;

    private volatile String responseString;

    public RpcEntity() {
        this(null, null);
    }

    public RpcEntity(String requestMethod, String requestData) {
        this(requestMethod, requestData, null);
    }

    public RpcEntity(String requestMethod, String requestData, String requestRelation) {
        this(requestMethod, requestData, requestRelation, null);
    }

    public RpcEntity(String requestMethod, String requestData, String requestRelation, Long requestTime) {
        this.requestThread = Thread.currentThread();
        this.requestMethod = requestMethod;
        this.requestData = requestData;
        this.requestRelation = requestRelation;
        this.requestTime = requestTime;
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
