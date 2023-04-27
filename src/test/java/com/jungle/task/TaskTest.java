package com.jungle.task;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TaskTest {
    @Test
    public void mockTest() {
        MockTaskManagement management = new MockTaskManagement(10);
        // 1. 获取库中的数据（数据库IO还好，害怕数据量太大造成堆压，再堆压的过程中可能会有重复的任务，能否去重剔除）
        management.registerConsumer().setPeriod(50L).setTimeUnit(TimeUnit.MILLISECONDS).start();
        management.registerConsumer((queue -> new MockTaskConsumer(queue) {
                    @Override
                    protected List<Integer> makeDemand(Long data) {
                        return Collections.singletonList(new Random().nextInt(100) * 2);
                    }
                })).setPeriod(50L)
                .setTimeUnit(TimeUnit.MILLISECONDS).start();
        management.registerWorkerLine(20L, TimeUnit.MILLISECONDS, true);
        management.startKeepAliveAndMonitor();
        TaskQueue taskQueue = management.getTaskQueue();
        while (true) {
        }

    }


}
