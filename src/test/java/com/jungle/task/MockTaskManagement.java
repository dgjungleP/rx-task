package com.jungle.task;

import lombok.Data;

@Data
public class MockTaskManagement extends AbstractTaskManagement<MockTaskConsumer> {
    public MockTaskManagement() {
        this(3);
    }

    public MockTaskManagement(Integer parallelism) {
        super(parallelism);
        TaskWorkerFactory.registerWorker(MockTask.class, new MockTaskWorker());
    }

    @Override
    public MockTaskConsumer doRegisterConsumer() {
        return MockTaskConsumer.registerConsumer(this.getTaskQueue());
    }
}
