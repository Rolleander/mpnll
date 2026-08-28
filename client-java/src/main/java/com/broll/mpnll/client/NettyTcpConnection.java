package com.broll.mpnll.client;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

final class NettyTcpConnection {

    private final EventLoopGroup workerGroup;
    private final ClientConnectionListener listener;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean openedReported = new AtomicBoolean();
    private final AtomicBoolean closedReported = new AtomicBoolean();
    private final AtomicBoolean errorReported = new AtomicBoolean();

    private ChannelFuture connectFuture;
    private volatile Channel channel;

    NettyTcpConnection(EventLoopGroup workerGroup, ClientConnectionListener listener) {
        this.workerGroup = workerGroup;
        this.listener = listener;
    }

    void open(TcpServerAddress address) {
        if (closed.get()) {
            return;
        }

        Bootstrap bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new NettyTcpChannelInitializer(this));

        try {
            ChannelFuture future = bootstrap.connect(address.host, address.port);
            synchronized (this) {
                if (closed.get()) {
                    future.cancel(false);
                    return;
                }
                connectFuture = future;
            }
            future.addListener((ChannelFutureListener) this::connectionCompleted);
        } catch (Throwable error) {
            failAndClose(error);
        }
    }

    void send(byte[] data) {
        Channel activeChannel = channel;
        if (activeChannel == null || !activeChannel.isActive()) {
            throw new IllegalStateException("Client is not connected");
        }
        activeChannel.writeAndFlush(Unpooled.wrappedBuffer(data))
            .addListener((ChannelFutureListener) result -> {
                if (!result.isSuccess()) {
                    reportError(result.cause());
                }
            });
    }

    boolean isConnected() {
        Channel activeChannel = channel;
        return activeChannel != null && activeChannel.isActive();
    }

    void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        ChannelFuture pending;
        Channel activeChannel;
        synchronized (this) {
            pending = connectFuture;
            connectFuture = null;
            activeChannel = channel;
            channel = null;
        }
        if (pending != null && !pending.isDone()) {
            pending.cancel(false);
        }
        if (activeChannel != null) {
            activeChannel.close();
        }
        reportClosed();
    }

    synchronized boolean connected(Channel newChannel) {
        if (closed.get()) {
            return false;
        }
        connectFuture = null;
        channel = newChannel;
        if (openedReported.compareAndSet(false, true)) {
            listener.onOpen();
        }
        return true;
    }

    void disconnected(Channel disconnectedChannel) {
        synchronized (this) {
            if (channel == disconnectedChannel) {
                channel = null;
            }
        }
        closed.set(true);
        reportClosed();
    }

    void receive(byte[] data) {
        if (!closed.get()) {
            listener.onMessage(data);
        }
    }

    void reportError(Throwable error) {
        if (!closed.get() && errorReported.compareAndSet(false, true)) {
            listener.onError(error);
        }
    }

    private void connectionCompleted(ChannelFuture result) {
        if (!result.isSuccess() && !result.isCancelled()) {
            failAndClose(result.cause());
        }
    }

    private void failAndClose(Throwable error) {
        reportError(error);
        close();
    }

    private void reportClosed() {
        if (closedReported.compareAndSet(false, true)) {
            listener.onClose();
        }
    }
}
