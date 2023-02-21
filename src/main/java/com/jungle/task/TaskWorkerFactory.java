package com.jungle.task;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TaskWorkerFactory {
    private TaskWorkerFactory() {
    }

    public static final Map<Class<? extends Task>, TaskWorker<? extends Task>> TASK_WORKER_MAP = new HashMap<>();


    public static Optional<TaskWorker<? extends Task>> tryGetWorker(Task task) {
        return Optional.ofNullable(TASK_WORKER_MAP.get(task.getClass()));
    }

    public static void registerWorker(Class<? extends Task> taskClazz, TaskWorker<? extends Task> worker) {
        if (!TASK_WORKER_MAP.containsKey(taskClazz)) {
            TASK_WORKER_MAP.put(taskClazz, worker);
        }
    }
}
