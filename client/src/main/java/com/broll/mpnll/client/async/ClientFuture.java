package com.broll.mpnll.client.async;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A small, GWT-compatible asynchronous result abstraction.
 */
public interface ClientFuture<T> {

    ClientFuture<T> onSuccess(Consumer<? super T> callback);

    ClientFuture<T> onFailure(Consumer<? super Throwable> callback);

    <R> ClientFuture<R> thenApply(Function<? super T, ? extends R> mapper);

    <R> ClientFuture<R> thenCompose(Function<? super T, ? extends ClientFuture<R>> mapper);

    boolean isDone();
}
