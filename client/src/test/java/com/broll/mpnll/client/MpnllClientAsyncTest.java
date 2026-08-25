package com.broll.mpnll.client;

import com.broll.mpnll.NtLobbyMessagesRegistry;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.async.ScheduledTask;
import com.broll.mpnll.client.impl.LobbyLookup;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.message.MessageUtils;
import com.broll.mpnll.nt.NT_ServerInformation;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MpnllClientAsyncTest {

    @After
    public void clearFactory() {
        NativeClientRegistry.NATIVE_CLIENT_FACTORY = null;
    }

    @Test
    public void completesOperationFromInboundMessageWithoutBlocking() {
        FakeNativeClient transport = new FakeNativeClient();
        MpnllClient client = new MpnllClient(transport);
        client.registerMessages(NtLobbyMessagesRegistry::register);

        ClientFuture<LobbyLookup> request = client.listLobbies("ws://localhost:8081/");
        AtomicReference<LobbyLookup> response = new AtomicReference<>();
        request.onSuccess(response::set);

        assertFalse(request.isDone());
        assertNotNull(transport.sent);

        MessageRegistryImpl registry = new MessageRegistryImpl();
        NtLobbyMessagesRegistry.register(registry::register);
        NT_ServerInformation message = NT_ServerInformation.newBuilder()
            .setServerName("test-server")
            .build();
        transport.receive(MessageUtils.toMessageBytes(registry, message));

        assertTrue(request.isDone());
        assertEquals("test-server", response.get().getServerName());
        assertTrue(transport.timeout.cancelled);
    }

    @Test
    public void waitsForAsynchronousConnectionBeforeSending() {
        FakeNativeClient transport = new FakeNativeClient(false);
        MpnllClient client = new MpnllClient(transport);
        client.registerMessages(NtLobbyMessagesRegistry::register);

        ClientFuture<LobbyLookup> request = client.listLobbies("ws://localhost:8081/");

        assertFalse(request.isDone());
        assertTrue(transport.sent == null);

        transport.finishOpen();

        assertNotNull(transport.sent);
        assertFalse(request.isDone());
    }

    @Test
    public void failsOperationWhenTimeoutRuns() {
        FakeNativeClient transport = new FakeNativeClient();
        MpnllClient client = new MpnllClient(transport);
        client.registerMessages(NtLobbyMessagesRegistry::register);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        ClientFuture<LobbyLookup> request = client.listLobbies("ws://localhost:8081/");
        request.onFailure(failure::set);
        transport.timeout.run();

        assertTrue(request.isDone());
        assertEquals("Operation timed out after 5 seconds", failure.get().getMessage());
    }

    private static class FakeNativeClient implements NativeClient {

        private final boolean openImmediately;
        private ClientConnectionListener listener;
        private boolean connected;
        private byte[] sent;
        private FakeScheduledTask timeout;

        private FakeNativeClient() {
            this(true);
        }

        private FakeNativeClient(boolean openImmediately) {
            this.openImmediately = openImmediately;
        }

        @Override
        public void open(String host, ClientConnectionListener listener) {
            this.listener = listener;
            if (openImmediately) {
                finishOpen();
            }
        }

        @Override
        public void close() {
            connected = false;
            if (listener != null) {
                listener.onClose();
            }
        }

        @Override
        public void send(byte[] data) {
            sent = data;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public ScheduledTask schedule(int delayMillis, Runnable action) {
            timeout = new FakeScheduledTask(action);
            return timeout;
        }

        @Override
        public void shutdown() {
        }

        private void receive(byte[] data) {
            listener.onMessage(data);
        }

        private void finishOpen() {
            connected = true;
            listener.onOpen();
        }
    }

    private static class FakeScheduledTask implements ScheduledTask {

        private final Runnable action;
        private boolean cancelled;

        private FakeScheduledTask(Runnable action) {
            this.action = action;
        }

        private void run() {
            if (!cancelled) {
                action.run();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
