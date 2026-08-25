package com.broll.mpnll.client;

import com.broll.mpnll.client.async.ScheduledTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MpnllTcpClient implements NativeClient {

    static {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = MpnllTcpClient::new;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Mpnll-client-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void open(String host, ClientConnectionListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void send(byte[] data) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public ScheduledTask schedule(int delayMillis, Runnable action) {
        ScheduledFuture<?> future = scheduler.schedule(action, delayMillis, TimeUnit.MILLISECONDS);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
