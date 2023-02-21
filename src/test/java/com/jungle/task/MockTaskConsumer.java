package com.jungle.task;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MockTaskConsumer extends AbstractTaskConsumer<Integer, MockTask> {
    private final Random random = new Random();


    @Override
    protected MockTask makeTask(Integer data) {
        return new MockTask(data);
    }

    @Override
    protected List<Integer> makeDemand(Long data) {
        return Collections.singletonList(random.nextInt(10));
    }

    public MockTaskConsumer(TaskQueue taskQueue) {
        super(taskQueue);
    }

    public static MockTaskConsumer registerConsumer(TaskQueue queue) {
        MockTaskConsumer consumer = new MockTaskConsumer(queue);
        consumer.registerConsumer();
        return consumer;
    }
}
