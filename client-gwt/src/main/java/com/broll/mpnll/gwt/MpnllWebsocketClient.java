package com.broll.mpnll.gwt;

import com.broll.mpnll.client.ClientConnectionListener;
import com.broll.mpnll.client.NativeClient;
import com.broll.mpnll.client.NativeClientRegistry;

public class MpnllWebsocketClient implements NativeClient {

    static {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = MpnllWebsocketClient::new;
    }

    private final GwtWebSocket socket = new GwtWebSocket();

    @Override
    public void open(String host, ClientConnectionListener listener) {
        socket.open(host);
        socket.setListener(new GwtWebSocket.Listener() {
            @Override
            public void onOpen() {
                listener.onOpen();
            }

            @Override
            public void onMessage(byte[] message) {
                listener.onMessage(message);
            }

            @Override
            public void onClose() {
                listener.onClose();
            }

            @Override
            public void onError(Throwable error) {
                listener.onError(error);
            }
        });
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public void send(byte[] data) {
        socket.send(data);
    }

    @Override
    public boolean isConnected() {
        return socket.isOpen();
    }
}
