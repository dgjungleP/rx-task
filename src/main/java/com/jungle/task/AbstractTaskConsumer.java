package com.jungle.task;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractTaskConsumer<I, M extends Task> implements TaskConsumer {
    private final TaskQueue taskQueue;
    private Flowable<M> flowable;
    private Disposable disposable;
    private Long period;
    private TimeUnit timeUnit;

    protected abstract M makeTask(I data);

    protected abstract List<I> makeDemand(Long data);


    public AbstractTaskConsumer(TaskQueue taskQueue) {
        this(taskQueue, 1L, TimeUnit.SECONDS);
    }

    public AbstractTaskConsumer(TaskQueue queue, Long period, TimeUnit timeUnit) {
        this.taskQueue = queue;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public void registerConsumer() {
        this.flowable = Flowable.interval(period, timeUnit).map(this::makeDemand).flatMap(Flowable::fromIterable)
                .map(this::makeTask).doOnNext(taskQueue::push);
    }

    public AbstractTaskConsumer<I, M> setPeriod(Long period) {
        this.period = period;
        return this;
    }

    public AbstractTaskConsumer<I, M> setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public void start() {
        if (this.disposable != null && !this.disposable.isDisposed()) {
            disposable.dispose();
        }
        startConsumer();
    }

    public void keepAlive() {
        if (this.disposable != null && this.disposable.isDisposed()) {
            log.info("The Consumer is died at:{}，have a try to restart.", Instant.now());
            startConsumer();
        }
    }

    private void startConsumer() {
        log.info("Start a Consumer at:{} by Period:【{}】  TimeUnit:【{}】", Instant.now(),
                this.period, this.timeUnit);
        this.registerConsumer();
        this.disposable = flowable.subscribe();
    }

}
