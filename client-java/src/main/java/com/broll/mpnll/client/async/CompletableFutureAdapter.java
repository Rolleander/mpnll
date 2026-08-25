package com.broll.mpnll.client.async;

import java.util.concurrent.CompletableFuture;

/**
 * Optional JVM adapter for applications that prefer CompletableFuture.
 */
public final class CompletableFutureAdapter {

    private CompletableFutureAdapter() {
    }

    public static <T> CompletableFuture<T> from(ClientFuture<T> source) {
        CompletableFuture<T> future = new CompletableFuture<>();
        source.onSuccess(future::complete);
        source.onFailure(future::completeExceptionally);
        return future;
    }
}
