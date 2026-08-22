package com.broll.mpnll.client;

public interface ClientStatusListener {

    void connected();

    void disconnected();

    void error(Throwable error);
}
