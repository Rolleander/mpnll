package com.broll.mpnll.client;

import com.broll.mpnll.client.async.ScheduledTask;
import com.broll.mpnll.client.persist.IFileAccess;
import com.broll.mpnll.client.persist.TempFileAccess;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class MpnllTcpClient implements NativeClient {

    static {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = MpnllTcpClient::new;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Mpnll-client-scheduler");
        thread.setDaemon(true);
        return thread;
    });
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1, runnable -> {
        Thread thread = new Thread(runnable, "Mpnll-client-network");
        thread.setDaemon(true);
        return thread;
    });

    private final Object stateLock = new Object();

    private volatile NettyTcpConnection connection;
    private boolean shutdown;

    @Override
    public void open(String host, ClientConnectionListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        TcpServerAddress address = TcpServerAddress.parse(host);
        NettyTcpConnection next = new NettyTcpConnection(workerGroup, listener);
        NettyTcpConnection previous;

        synchronized (stateLock) {
            if (shutdown) {
                throw new IllegalStateException("Client has been shut down");
            }
            previous = connection;
            connection = next;
        }
        close(previous);
        next.open(address);
    }

    @Override
    public void close() {
        NettyTcpConnection current;
        synchronized (stateLock) {
            current = connection;
            connection = null;
        }
        close(current);
    }

    @Override
    public void send(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        NettyTcpConnection current = connection;
        if (current == null || !current.isConnected()) {
            throw new IllegalStateException("Client is not connected");
        }
        current.send(data);
    }

    @Override
    public boolean isConnected() {
        NettyTcpConnection current = connection;
        return current != null && current.isConnected();
    }

    @Override
    public ScheduledTask schedule(int delayMillis, Runnable action) {
        ScheduledFuture<?> future = scheduler.schedule(action, delayMillis, TimeUnit.MILLISECONDS);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdown() {
        NettyTcpConnection current;
        synchronized (stateLock) {
            if (shutdown) {
                return;
            }
            shutdown = true;
            current = connection;
            connection = null;
        }
        close(current);
        workerGroup.shutdownGracefully();
        scheduler.shutdownNow();
    }

    @Override
    public IFileAccess lastConnectionAccess() {
        return new TempFileAccess("mpnll-last-connection.dat");
    }

    @Override
    public IFileAccess clientAuthAccess() {
        return new TempFileAccess("mpnll-user-auth.dat");
    }

    private void close(NettyTcpConnection connection) {
        if (connection != null) {
            connection.close();
        }
    }
}
