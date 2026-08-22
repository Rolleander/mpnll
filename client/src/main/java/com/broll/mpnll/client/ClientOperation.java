package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.persist.ClientAuthentication;
import com.broll.mpnll.client.site.ClientSite;
import com.google.protobuf.Message;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ClientOperation<T> {

    private final static int TIMEOUT = 5;
    private CompletableFuture<T> future = new CompletableFuture<>();
    private List<Runnable> cleanup = new ArrayList<>();
    private MpnllClient client;

    CompletableFuture<T> run(MpnllClient client) {
        this.client = client;
        this.future = new CompletableFuture<>();
        try {
            operation();
            complete(waitFor(this.future));
        } catch (Exception e) {
            fail(e);
        }
        return future;
    }

    protected abstract void operation();

    protected void register(ClientSite site) {
        this.client.addSite(site);
        this.cleanup.add(() -> this.client.removeSite(site));
    }

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

    protected void send(Message message) {
        this.client.send(message);
    }

    private <F> F waitFor(Future<F> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new NetworkException(e);
        }
    }

    protected void complete(T result) {
        cleanup.forEach(Runnable::run);
        future.complete(result);
    }

    public void fail(Exception e) {
        cleanup.forEach(Runnable::run);
        future.completeExceptionally(e);
    }
}
