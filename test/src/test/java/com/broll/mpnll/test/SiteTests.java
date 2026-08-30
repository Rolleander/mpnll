package com.broll.mpnll.test;

import static org.junit.Assert.assertEquals;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;
import com.broll.mpnll.server.MpnllServer;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.site.PackageReceiver;
import com.broll.mpnll.server.utils.Autoshared;
import com.broll.mpnll.server.utils.ConnectionRestriction;
import com.broll.mpnll.server.utils.RestrictionType;
import com.broll.mpnll.server.utils.ShareLevel;
import com.google.protobuf.StringValue;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SiteTests extends MpnllIntegrationTest {

    private static final StringValue TEST_MESSAGE = StringValue.of("site-test");

    private MpnllClient client;

    @Override
    protected void configureServer(MpnllServer server) {
        server.registerMessages(registry -> registry.register(StringValue.newBuilder()));
    }

    @Override
    protected void configureClient(MpnllClient client) {
        client.registerMessages(registry -> registry.register(StringValue.newBuilder()));
    }

    @Before
    public void connectClient() throws Exception {
        client = newClient("site-client");
        // A TCP connect can become active on the client just before the server's
        // channelActive callback runs. Complete one request before testing pushes.
        await(client.listLobbies());
    }

    @Test
    public void invokesUnknownMessageReceivers() throws Exception {
        CompletableFuture<StringValue> clientUnknown = new CompletableFuture<>();
        client.setUnknownMessageReceiver(message -> clientUnknown.complete((StringValue) message));

        server.sendToAll(TEST_MESSAGE);

        assertEquals(TEST_MESSAGE, clientUnknown.get(5, TimeUnit.SECONDS));

        CompletableFuture<StringValue> serverUnknown = new CompletableFuture<>();
        server.setUnknownMessageReceiver(message -> serverUnknown.complete((StringValue) message));

        client.send(TEST_MESSAGE);

        assertEquals(TEST_MESSAGE, serverUnknown.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void registersAndRemovesClientSite() throws Exception {
        CompletableFuture<StringValue> received = new CompletableFuture<>();
        CompletableFuture<StringValue> unknown = new CompletableFuture<>();
        client.setUnknownMessageReceiver(message -> unknown.complete((StringValue) message));
        ClientSite site = new ClientSite() {
            @Override
            protected void registerReceivers(MessageReceiverRegistry registry) {
                registry.connect(StringValue.class, received::complete);
            }
        };

        client.addSite(site);
        server.sendToAll(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, received.get(5, TimeUnit.SECONDS));

        client.removeSite(site);
        server.sendToAll(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, unknown.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void registersAndRemovesServerSite() throws Exception {
        CompletableFuture<StringValue> received = new CompletableFuture<>();
        CompletableFuture<StringValue> unknown = new CompletableFuture<>();
        server.setUnknownMessageReceiver(message -> unknown.complete((StringValue) message));
        TestServerSite site = new TestServerSite(received);

        server.addSite(site);
        client.send(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, received.get(5, TimeUnit.SECONDS));

        server.removeSite(site);
        client.send(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, unknown.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void injectsAutosharedFieldsIntoClonedSite() throws Exception {
        CompletableFuture<Integer> received = new CompletableFuture<>();
        server.addSite(new AutosharedServerSite(received));

        client.send(TEST_MESSAGE);

        assertEquals(Integer.valueOf(1), received.get(5, TimeUnit.SECONDS));
    }

    public static class TestServerSite extends NetworkSite {

        private CompletableFuture<StringValue> received;

        public TestServerSite() {
        }

        TestServerSite(CompletableFuture<StringValue> received) {
            this.received = received;
        }

        @PackageReceiver
        @ConnectionRestriction(RestrictionType.NONE)
        public void received(StringValue message) {
            received.complete(message);
        }
    }

    public static class AutosharedServerSite extends NetworkSite {

        @Autoshared(ShareLevel.SERVER)
        private SharedCounter counter;
        private CompletableFuture<Integer> received;

        public AutosharedServerSite() {
        }

        AutosharedServerSite(CompletableFuture<Integer> received) {
            this.received = received;
        }

        @PackageReceiver
        @ConnectionRestriction(RestrictionType.NONE)
        public void received(StringValue message) {
            received.complete(++counter.value);
        }
    }

    public static class SharedCounter {
        private int value;
    }
}
