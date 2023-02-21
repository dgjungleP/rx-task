package com.jungle.task.entity;

import io.reactivex.rxjava3.core.Observable;//如果用的是RxJava2的请改为该版本的包名
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Optional;

public class RxLiveData<T> {
    private final Observable<T> observable;
    private Disposable disposable;
    private T value;
    private ObservableEmitter<T> emitter;

    public RxLiveData() {
        observable = Observable
                .create((ObservableEmitter<T> emitter) -> this.emitter = emitter)
                .publish()
                .autoConnect(0, disposable -> this.disposable = disposable);
    }

    public Observable<T> getObservable() {
        return observable;
    }

    public void postData(T value) {
        this.value = value;
        if (emitter != null && value != null) {
            emitter.onNext(value);
        }
    }

    public T getValue() {
        return value;
    }

    public Optional<T> optValue() {
        return Optional.ofNullable(value);
    }
}
