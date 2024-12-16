package com.broll.mpnll.client;

public interface NativeClient {

    void open(String host, ClientConnectionListener listener);

    void close();

    void send(byte[] data);

    boolean isConnected();
}
