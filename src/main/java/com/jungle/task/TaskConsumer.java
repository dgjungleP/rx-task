package com.jungle.task;

import io.reactivex.rxjava3.core.Flowable;

public interface TaskConsumer {
    void registerConsumer();

    void start();

    public void keepAlive();

}
