package com.broll.mpnll.client.async;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Completes a {@link ClientFuture} without blocking the current thread/event loop.
 */
public final class ClientPromise<T> implements ClientFuture<T> {

    private final List<Consumer<? super T>> successCallbacks = new ArrayList<>();
    private final List<Consumer<? super Throwable>> failureCallbacks = new ArrayList<>();
    private boolean done;
    private T value;
    private Throwable error;

    public static <T> ClientFuture<T> completed(T value) {
        ClientPromise<T> promise = new ClientPromise<>();
        promise.complete(value);
        return promise;
    }

    public static <T> ClientFuture<T> failed(Throwable error) {
        ClientPromise<T> promise = new ClientPromise<>();
        promise.fail(error);
        return promise;
    }

    public void complete(T value) {
        List<Consumer<? super T>> callbacks;
        synchronized (this) {
            if (done) {
                return;
            }
            done = true;
            this.value = value;
            callbacks = new ArrayList<>(successCallbacks);
            successCallbacks.clear();
            failureCallbacks.clear();
        }
        callbacks.forEach(callback -> callback.accept(value));
    }

    public void fail(Throwable error) {
        if (error == null) {
            error = new RuntimeException("Asynchronous operation failed");
        }
        List<Consumer<? super Throwable>> callbacks;
        synchronized (this) {
            if (done) {
                return;
            }
            done = true;
            this.error = error;
            callbacks = new ArrayList<>(failureCallbacks);
            successCallbacks.clear();
            failureCallbacks.clear();
        }
        Throwable completedError = error;
        callbacks.forEach(callback -> callback.accept(completedError));
    }

    @Override
    public ClientFuture<T> onSuccess(Consumer<? super T> callback) {
        T completedValue;
        synchronized (this) {
            if (!done) {
                successCallbacks.add(callback);
                return this;
            }
            if (error != null) {
                return this;
            }
            completedValue = value;
        }
        callback.accept(completedValue);
        return this;
    }

    @Override
    public ClientFuture<T> onFailure(Consumer<? super Throwable> callback) {
        Throwable completedError;
        synchronized (this) {
            if (!done) {
                failureCallbacks.add(callback);
                return this;
            }
            if (error == null) {
                return this;
            }
            completedError = error;
        }
        callback.accept(completedError);
        return this;
    }

    @Override
    public <R> ClientFuture<R> thenApply(Function<? super T, ? extends R> mapper) {
        ClientPromise<R> next = new ClientPromise<>();
        onSuccess(value -> {
            try {
                next.complete(mapper.apply(value));
            } catch (Throwable error) {
                next.fail(error);
            }
        });
        onFailure(next::fail);
        return next;
    }

    @Override
    public <R> ClientFuture<R> thenCompose(Function<? super T, ? extends ClientFuture<R>> mapper) {
        ClientPromise<R> next = new ClientPromise<>();
        onSuccess(value -> {
            try {
                ClientFuture<R> mapped = mapper.apply(value);
                if (mapped == null) {
                    next.fail(new NullPointerException("Async mapper returned null"));
                    return;
                }
                mapped.onSuccess(next::complete).onFailure(next::fail);
            } catch (Throwable error) {
                next.fail(error);
            }
        });
        onFailure(next::fail);
        return next;
    }

    @Override
    public synchronized boolean isDone() {
        return done;
    }
}
