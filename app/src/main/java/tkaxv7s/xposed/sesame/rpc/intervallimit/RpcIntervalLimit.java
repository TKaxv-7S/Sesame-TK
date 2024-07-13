package tkaxv7s.xposed.sesame.rpc.intervallimit;

import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.TimeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcIntervalLimit {

    private static final IntervalLimit defaultIntervalLimit = new DefaultIntervalLimit(50);

    private static final Map<String, IntervalLimit> intervalLimitMap = new ConcurrentHashMap<>();

    public static synchronized void addIntervalLimit(String method, Integer interval) {
        addIntervalLimit(method, new DefaultIntervalLimit(interval));
    }

    public static synchronized void addIntervalLimit(String method, IntervalLimit intervalLimit) {
        if (intervalLimitMap.containsKey(method)) {
            Log.i("方法：" + method + " 间隔限制已存在");
            throw new IllegalArgumentException("方法：" + method + " 间隔限制已存在");
        }
        intervalLimitMap.put(method, intervalLimit);
    }

    public static synchronized void updateIntervalLimit(String method, Integer interval) {
        updateIntervalLimit(method, new DefaultIntervalLimit(interval));
    }

    public static synchronized void updateIntervalLimit(String method, IntervalLimit intervalLimit) {
        intervalLimitMap.put(method, intervalLimit);
    }

    public static void enterIntervalLimit(String method) {
        IntervalLimit intervalLimit = intervalLimitMap.get(method);
        if (intervalLimit == null) {
            intervalLimit = defaultIntervalLimit;
        }
        synchronized (intervalLimit) {
            long sleep = intervalLimit.getInterval() - System.currentTimeMillis() + intervalLimit.getTime();
            if (sleep > 0) {
                TimeUtil.sleep(sleep);
            }
            intervalLimit.setTime(System.currentTimeMillis());
        }
    }

    public static synchronized void clearIntervalLimit() {
        intervalLimitMap.clear();
    }

}
