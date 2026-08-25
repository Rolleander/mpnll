package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.async.ClientPromise;
import com.broll.mpnll.client.async.ScheduledTask;
import com.broll.mpnll.client.persist.ClientAuthentication;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;
import com.google.protobuf.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ClientOperation<T> {

    private final static int TIMEOUT = 5;
    private MpnllClient client;

    ClientFuture<T> run(MpnllClient client) {
        this.client = client;
        try {
            return operation();
        } catch (Throwable error) {
            return ClientPromise.failed(error);
        }
    }

    protected abstract ClientFuture<T> operation();

    protected void requireConnected() {
        if (!client.isConnected()) {
            throw new NetworkException("Client must be connected for this operation");
        }
    }

    protected ClientFuture<Void> connect(String ip) {
        if (client.isConnected()) {
            if (!java.util.Objects.equals(client.getHost(), ip)) {
                client.close();
                return client.openAsync(ip);
            }
            return ClientPromise.completed(null);
        }
        return client.openAsync(ip);
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

    public class AwaitResponseBuilder<R> {

        private Message message;
        private Map<Class<?>, Function<Message, R>> calls = new HashMap<>();

        AwaitResponseBuilder(Message message) {
            this.message = message;
        }

        public <M extends Message> AwaitResponseBuilder<R> on(Message.Builder messageType, Function<M, R> call) {
            calls.put(messageType.getDefaultInstanceForType().getClass(), (Function<Message, R>) call);
            return this;
        }

        public ClientFuture<R> execute() {
            ClientPromise<R> result = new ClientPromise<>();
            ClientSite site = new ClientSite() {
                @Override
                protected void registerReceivers(MessageReceiverRegistry registry) {
                    calls.forEach((messageType, call) -> registry.connect(messageType, message -> {
                        try {
                            result.complete(call.apply(message));
                        } catch (Throwable error) {
                            result.fail(error);
                        }
                    }));
                }
            };
            client.addSite(site);
            ScheduledTask timeout = client.schedule(TIMEOUT * 1000, () ->
                result.fail(new NetworkException("Operation timed out after " + TIMEOUT + " seconds"))
            );
            result.onSuccess(value -> {
                timeout.cancel();
                client.removeSite(site);
            });
            result.onFailure(error -> {
                timeout.cancel();
                client.removeSite(site);
            });
            try {
                client.send(message);
            } catch (Throwable error) {
                result.fail(error);
            }
            return result;
        }
    }

}
