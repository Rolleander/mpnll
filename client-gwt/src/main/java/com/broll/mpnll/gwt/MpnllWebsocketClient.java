package com.broll.mpnll.gwt;

import com.broll.mpnll.client.ClientConnectionListener;
import com.broll.mpnll.client.NativeClient;
import com.broll.mpnll.client.NativeClientRegistry;
import com.broll.mpnll.client.async.ScheduledTask;
import com.broll.mpnll.client.persist.IFileAccess;
import com.google.gwt.user.client.Timer;

public class MpnllWebsocketClient implements NativeClient {

    static {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = MpnllWebsocketClient::new;
    }

    private final GwtWebSocket socket = new GwtWebSocket();

    @Override
    public void open(String host, ClientConnectionListener listener) {
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
        socket.open(host);
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

    @Override
    public ScheduledTask schedule(int delayMillis, Runnable action) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                action.run();
            }
        };
        timer.schedule(delayMillis);
        return timer::cancel;
    }

    @Override
    public void shutdown() {
        // GWT timers are individually cancelled by completed operations.
    }

    @Override
    public IFileAccess clientAuthAccess() {
        return new LocalStorageFileAccess("mpnll-user-auth");
    }

    @Override
    public IFileAccess lastConnectionAccess() {
        return new LocalStorageFileAccess("mpnll-last-connection");
    }
}
