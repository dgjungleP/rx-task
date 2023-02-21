package com.jungle.task;

public interface TaskWorker<T extends Task> {

    void doWork(T task);

    void tryWork(Task task);

}
