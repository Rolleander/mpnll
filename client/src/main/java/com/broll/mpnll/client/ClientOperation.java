package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.persist.ClientAuthentication;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;
import com.google.protobuf.Message;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public abstract class ClientOperation<T> {

    private final static int TIMEOUT = 5;
    private CompletableFuture<T> future = new CompletableFuture<>();
    private MpnllClient client;

    CompletableFuture<T> run(MpnllClient client) {
        this.client = client;
        this.future = new CompletableFuture<>();
        operation();
        waitFor(this.future);
        return future;
    }

    protected abstract void operation();

    protected void requireConnected() {
        if (!client.isConnected()) {
            throw new NetworkException("Client must be connected for this operation");
        }
    }

    protected void connect(String ip) {
        if (client.isConnected()) {
            if (!StringUtils.equals(client.getHost(), ip)) {
                client.close();
                client.open(ip);
            }
        } else {
            client.open(ip);
        }
    }

    protected ClientAuthentication getClientAuthentication() {
        return client.getClientAuthentication();
    }

    protected String getClientVersion() {
        return client.getVersion();
    }

    protected String getConnectedIp() {
        requireConnected();
        return client.getHost();
    }

    protected <R> AwaitResponseBuilder<R> send(Message message) {
        return new AwaitResponseBuilder<>(message);
    }

    protected MpnllClient getClient() {
        return client;
    }

    private <F> F waitFor(Future<F> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new NetworkException(e);
        }
    }

    protected void complete(T result) {
        future.complete(result);
    }

    public class AwaitResponseBuilder<R> {

        private Message message;
        private Map<Message.Builder, Function<Message, R>> calls = new HashMap<>();
        private CompletableFuture<R> future = new CompletableFuture<>();

        AwaitResponseBuilder(Message message) {
            this.message = message;
        }

        public AwaitResponseBuilder<R> on(Message.Builder messageType, Function<? extends Message, R> call) {
            calls.put(messageType, (Function<Message, R>) call);
            return this;
        }

        public R awaitResponse() {
            ClientSite site = new ClientSite() {
                @Override
                protected void registerReceivers(MessageReceiverRegistry registry) {
                    calls.forEach((messageBuilder, call) ->
                        registry.connect(messageBuilder, (message) -> {
                            future.complete(call.apply(message));
                        })
                    );
                }
            };
            client.addSite(site);
            client.send(message);
            try {
                return waitFor(future);
            } finally {
                client.removeSite(site);
            }
        }
    }

}
