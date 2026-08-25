package com.broll.mpnll.client;

import com.broll.mpnll.client.async.ScheduledTask;
import com.broll.mpnll.client.persist.IFileAccess;

public interface NativeClient {

    void open(String host, ClientConnectionListener listener);

    void close();

    void send(byte[] data);

    boolean isConnected();

    ScheduledTask schedule(int delayMillis, Runnable action);

    void shutdown();

    IFileAccess clientAuthAccess();
    
    IFileAccess lastConnectionAccess();
}
