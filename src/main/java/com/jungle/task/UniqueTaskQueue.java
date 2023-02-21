package com.jungle.task;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class UniqueTaskQueue implements TaskQueue {

    public final Queue<Task> TASK_QUEUE = new ConcurrentLinkedDeque<>();

    @Override
    public void push(Task task) {
        if (!TASK_QUEUE.contains(task)) {
            TASK_QUEUE.add(task);
        }
    }

    @Override
    public Optional<Task> take() {

        return Optional.ofNullable(TASK_QUEUE.poll());
    }

    @Override
    public List<Optional<Task>> take(int num) {
        List<Optional<Task>> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            result.add(take());
        }
        return result;
    }

    @Override
    public String toString() {
        return "UniqueTaskQueue{" +
                "TASK_QUEUE=" + TASK_QUEUE +
                '}';
    }
}