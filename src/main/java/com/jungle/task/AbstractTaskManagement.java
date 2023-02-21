package com.jungle.task;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Data
public abstract class AbstractTaskManagement<C extends TaskConsumer> implements TaskManagement<C, UniqueTaskQueue> {
    private UniqueTaskQueue taskQueue = new UniqueTaskQueue();
    private List<C> consumerList = new ArrayList<>();
    private Disposable disposable;
    private Integer PARALLELISM = 3;

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
        if (this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
        }
        this.disposable = Flowable.interval(period, timeUnit).onBackpressureDrop()
                .map(data -> taskQueue.take(PARALLELISM)).flatMap(Flowable::fromIterable)
                .groupBy(data -> data.orElseGet(EmptyTask::new).getClass()).forEach(flow -> makeFlow(flow, sync));
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

}
