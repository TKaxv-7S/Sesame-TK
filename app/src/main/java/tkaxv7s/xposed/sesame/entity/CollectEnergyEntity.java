package tkaxv7s.xposed.sesame.entity;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
public class CollectEnergyEntity {

    private final String userId;

    @Setter
    private JSONObject userHome;

    @Setter
    private RpcEntity rpcEntity;

    private Integer collectCount = 0;

    private Integer tryCount = 0;

    @Setter
    private Boolean needDouble = false;

    @Setter
    private Boolean needRetry = false;

    public CollectEnergyEntity(String userId) {
        this.userId = userId;
    }

    public CollectEnergyEntity(String userId, JSONObject userHome) {
        this.userId = userId;
        this.userHome = userHome;
    }

    public CollectEnergyEntity(String userId, JSONObject userHome, RpcEntity rpcEntity) {
        this.userId = userId;
        this.userHome = userHome;
        this.rpcEntity = rpcEntity;
    }

    public Integer addTryCount() {
        this.tryCount = tryCount + 1;
        return tryCount;
    }

    public void resetTryCount() {
        this.tryCount = 0;
    }

    public void setNeedDouble() {
        this.collectCount = collectCount + 1;
        this.needDouble = true;
    }

    public void unsetNeedDouble() {
        this.needDouble = false;
    }

    public void setNeedRetry() {
        this.needRetry = true;
    }

    public void unsetNeedRetry() {
        this.needRetry = false;
    }

}
