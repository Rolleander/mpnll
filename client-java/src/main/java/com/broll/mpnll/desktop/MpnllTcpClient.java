package com.broll.mpnll.desktop;

import com.broll.mpnll.client.ClientConnectionListener;
import com.broll.mpnll.client.NativeClient;
import com.broll.mpnll.client.NativeClientRegistry;

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
