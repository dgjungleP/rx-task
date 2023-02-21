package com.jungle.task;


import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MockTaskWorker extends AbstractTaskWorker<MockTask> {
    public MockTaskWorker() {
    }

    @Override
    public void doWork(MockTask task) {
        try {

            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(new Date() + ":" + task.getId());
    }

}
