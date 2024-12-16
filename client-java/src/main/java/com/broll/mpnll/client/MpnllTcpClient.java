package com.broll.mpnll.client;

public class MpnllTcpClient implements NativeClient {

    static {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = MpnllTcpClient::new;
    }

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
}
