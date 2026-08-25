package com.broll.mpnll.client.async;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClientPromiseTest {

    @Test
    public void deliversCompletionBeforeAndAfterCallbackRegistration() {
        ClientPromise<String> promise = new ClientPromise<>();
        AtomicReference<String> early = new AtomicReference<>();
        AtomicReference<String> late = new AtomicReference<>();

        promise.onSuccess(early::set);
        promise.complete("done");
        promise.onSuccess(late::set);

        assertTrue(promise.isDone());
        assertEquals("done", early.get());
        assertEquals("done", late.get());
    }

    @Test
    public void mapsAndComposesWithoutBlocking() {
        ClientPromise<Integer> source = new ClientPromise<>();
        ClientPromise<Integer> nested = new ClientPromise<>();
        AtomicReference<String> value = new AtomicReference<>();

        source.thenApply(number -> number + 1)
            .thenCompose(number -> nested.thenApply(next -> number + next))
            .thenApply(Object::toString)
            .onSuccess(value::set);

        source.complete(4);
        assertNull(value.get());
        nested.complete(5);
        assertEquals("10", value.get());
    }

    @Test
    public void propagatesMapperFailures() {
        ClientPromise<Integer> source = new ClientPromise<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        source.<Integer>thenApply(value -> {
            throw new IllegalStateException("broken");
        }).onFailure(failure::set);

        source.complete(1);

        assertEquals("broken", failure.get().getMessage());
    }
}
