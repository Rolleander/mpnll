package com.broll.mpnll.client;

public interface NativeClient {

    void open(String host, ClientConnectionListener listener);

    void close();

    boolean isConnected();
}
