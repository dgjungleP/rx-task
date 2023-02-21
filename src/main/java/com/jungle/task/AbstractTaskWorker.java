package com.jungle.task;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractTaskWorker<T extends Task> implements TaskWorker<T> {
    private Class<T> clazz;

    public AbstractTaskWorker() {
        this.clazz = (Class<T>) ((ParameterizedType) this.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public void tryWork(Task task) {
        if (task.getClass().equals(clazz)) {
            doWork((T) task);
        }
    }
}
