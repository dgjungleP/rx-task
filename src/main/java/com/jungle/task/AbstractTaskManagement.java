package com.jungle.task;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Data
@Slf4j
public abstract class AbstractTaskManagement<C extends TaskConsumer> implements TaskManagement<C, UniqueTaskQueue> {
    private UniqueTaskQueue taskQueue = new UniqueTaskQueue();
    private List<C> consumerList = new ArrayList<>();
    private Disposable workerDisposable;
    private Disposable checkAliveDisposable;
    private Integer PARALLELISM = 3;
    private WorkerConfigHolder workerConfigHolder;

    public AbstractTaskManagement(List<C> consumerList) {
        this.consumerList = consumerList;
    }

    public AbstractTaskManagement(Integer parallelism) {
        this.PARALLELISM = parallelism;
    }

    public AbstractTaskManagement() {
    }

    public void registerWorkerLine() {
        registerWorkerLine(100L, TimeUnit.MILLISECONDS, false);
    }

    public void registerWorkerLine(Long period, TimeUnit timeUnit) {
        registerWorkerLine(period, timeUnit, false);
    }

    public void registerWorkerLine(Long period, TimeUnit timeUnit, Boolean sync) {
        if (workerConfigHolder == null) {
            workerConfigHolder = new WorkerConfigHolder();
        }
        workerConfigHolder.flush(period, timeUnit, sync);
        if (this.workerDisposable != null && !this.workerDisposable.isDisposed()) {
            this.workerDisposable.dispose();
        }
        startWorker();
    }

    private void keepAlive() {
        try {
            if (this.workerDisposable != null && this.workerDisposable.isDisposed()) {
                log.info("The Worker is died at:{}，have a try to restart.", Instant.now());
                startWorker();
            }
            this.consumerList.forEach(TaskConsumer::keepAlive);
        } catch (Exception e) {
            log.error("The KeepAlive is died at:{}，Please Retry update", Instant.now());
            doOnKeepAliveException();
        }
    }

    public void doOnKeepAliveException() {
    }


    private void startWorker() {
        log.info("Start a Worker at:{} by Period:【{}】  TimeUnit:【{}】  Sync:【{}】 ", Instant.now(), workerConfigHolder.period, workerConfigHolder.timeUnit, workerConfigHolder.sync);
        this.workerDisposable = Flowable.interval(workerConfigHolder.period, workerConfigHolder.timeUnit)
                .onBackpressureDrop().map(data -> taskQueue.take(PARALLELISM)).flatMap(Flowable::fromIterable)
                .groupBy(data -> data.orElseGet(EmptyTask::new).getClass())
                .forEach(flow -> makeFlow(flow, workerConfigHolder.sync));
    }

    private Disposable makeFlow(GroupedFlowable<? extends Class<? extends Task>, Optional<Task>> flow, Boolean sync) {
        if (sync) {
            return flow.parallel(PARALLELISM).runOn(Schedulers.io())
                    .doOnNext(data -> data.ifPresent(task -> TaskWorkerFactory.tryGetWorker(task)
                            .ifPresent(worker -> worker.tryWork(task)))).sequential().subscribe();
        }
        return flow.subscribe(data -> data.ifPresent(task -> TaskWorkerFactory.tryGetWorker(task)
                .ifPresent(worker -> worker.tryWork(task))));
    }

    public abstract C doRegisterConsumer();

    public C registerConsumer() {
        return registerConsumer((queue) -> doRegisterConsumer());
    }

    public C registerConsumer(Function<TaskQueue, C> function) {
        C consumer = function.apply(this.taskQueue);
        this.getConsumerList().add(consumer);
        return consumer;
    }

    public void startKeepAliveAndMonitor() {
        this.checkAliveDisposable = Flowable.interval(1, TimeUnit.MINUTES)
                .onBackpressureDrop().subscribe((data) -> this.keepAlive());
    }

    @Data
    public static class WorkerConfigHolder {
        private Long period;
        private TimeUnit timeUnit;
        private Boolean sync;

        public void flush(Long period, TimeUnit timeUnit, Boolean sync) {
            this.period = period;
            this.timeUnit = timeUnit;
            this.sync = sync;
        }
    }
}
