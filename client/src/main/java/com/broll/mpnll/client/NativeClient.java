package com.broll.mpnll.client;

import com.broll.mpnll.client.async.ScheduledTask;

public interface NativeClient {

    void open(String host, ClientConnectionListener listener);

    void close();

    void send(byte[] data);

    boolean isConnected();

    ScheduledTask schedule(int delayMillis, Runnable action);

    void shutdown();
}
