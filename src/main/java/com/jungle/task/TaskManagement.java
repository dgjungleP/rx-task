package com.jungle.task;


public interface TaskManagement<C extends TaskConsumer, Q extends TaskQueue> {

    void registerWorkerLine();

    C registerConsumer();

    Q getTaskQueue();

}
