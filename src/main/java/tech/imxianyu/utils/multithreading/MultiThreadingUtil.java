package tech.imxianyu.utils.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ImXianyu
 * @since 2022/5/17 17:57
 */
public class MultiThreadingUtil {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    public static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(10, 30, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r ->
            new Thread(r, String.format("Thread %s", COUNTER.incrementAndGet())));
    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(10, r ->
            new Thread(r, "Thread " + COUNTER.incrementAndGet()));

    public static ScheduledFuture<?> schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        return MultiThreadingUtil.RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        return MultiThreadingUtil.RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static ScheduledFuture<?> scheduledWithFixedRate(Runnable r, long delay, TimeUnit unit) {
        return MultiThreadingUtil.RUNNABLE_POOL.scheduleWithFixedDelay(r, 0, delay, unit);
    }

    public static void runAsync(Runnable runnable) {
        MultiThreadingUtil.POOL.execute(runnable);
    }

    public static Future<?> submit(Runnable runnable) {
        return MultiThreadingUtil.POOL.submit(runnable);
    }
}
