package com.broll.mpnll.client;

public interface ClientConnectionListener {

    void onOpen();

    void onClose();

    void onError(Throwable error);

    void onMessage(byte[] data);
}
