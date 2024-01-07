package tech.imxianyu.utils.debugging;

import lombok.Getter;

public class Profiler {
    @Getter
    private static String currentProfiling = "";

    @Getter
    private static long lastMs;

    @Getter
    private static long taskUsedTime;

    public Profiler() {
        Profiler.lastMs = System.currentTimeMillis();
    }

    public static void start(String profile) {
        Profiler.currentProfiling = profile;
        Profiler.lastMs = System.currentTimeMillis();
    }

    public static void endStartTask(String profile) {
        Profiler.endTask();
        Profiler.start(profile);
    }

    public static void endTask() {
        Profiler.taskUsedTime = System.currentTimeMillis() - lastMs;
    }

    public static void print() {
        System.out.println("Task " + currentProfiling + " used " + taskUsedTime + " ms.");
    }
}
