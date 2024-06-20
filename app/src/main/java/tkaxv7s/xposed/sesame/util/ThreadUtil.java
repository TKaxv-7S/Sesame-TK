package tkaxv7s.xposed.sesame.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    private static final String TAG = ThreadUtil.class.getSimpleName();

    public static void shutdownAndWait(Thread thread, long timeout, TimeUnit unit) {
        if (thread != null) {
            thread.interrupt();
            if (timeout > -1L) {
                try {
                    thread.join(unit.toMillis(timeout));
                } catch (InterruptedException e) {
                    Log.i(TAG, "thread shutdownAndWait err:");
                    Log.printStackTrace(TAG, e);
                }
            }
        }
    }

    public boolean shutdownNow(ExecutorService pool) {
        try {
            shutdownAndAwaitTermination(pool, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.i(TAG, "thread shutdownAndWait err:");
            Log.printStackTrace(TAG, e);
            return false;
        }
        return true;
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool, long timeout, TimeUnit unit) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(timeout, unit)) {
                        Log.i(TAG, "thread pool can't close");
                    }
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
