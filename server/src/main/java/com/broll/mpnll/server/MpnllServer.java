package com.broll.mpnll.server;

import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.message.MessageUtils;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.connection.ClientConnectionRegistry;
import com.broll.mpnll.server.connection.ClientConnectionRegistryImpl;
import com.broll.mpnll.server.inbound.SetupContext;
import com.broll.mpnll.server.inbound.TcpServerSetup;
import com.broll.mpnll.server.inbound.WebsocketServerSetup;
import com.broll.mpnll.server.lobby.LobbyHandler;
import com.broll.mpnll.server.site.CloningSitesHandler;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.site.SitesHandler;
import com.broll.mpnll.server.utils.SharedData;
import com.google.protobuf.Message;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Consumer;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class MpnllServer {

    private final MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private final LobbyHandler lobbyHandler = new LobbyHandler(messageRegistry);
    private final SitesHandler sitesHandler = new CloningSitesHandler();
    private final ClientConnectionRegistry sessionRegistry = new ClientConnectionRegistryImpl(messageRegistry);
    private Channel tcpChannel, wsChannel;
    private EventLoopGroup bossGroup, workerGroup;
    private boolean open = false;
    private SharedData sharedData = new SharedData();
    private String customName;
    private String version = "1.0";
    private String ip;

    public MpnllServer() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return customName != null ? customName : ip;
    }

    public void setName(String name) {
        this.customName = name;
    }

    public String getIp() {
        return ip;
    }

    public Map<Class<NetworkSite>, NetworkSite> getSiteInstances(ClientConnection connection) {
        return sitesHandler.getSiteInstances(connection);
    }

    public void registerMessages(Consumer<MessageRegistrySetup> registry) {
        registry.accept(messageRegistry::register);
    }

    public void addSite(NetworkSite site) {
        sitesHandler.add(site);
        site.init(this, lobbyHandler);
    }

    public void removeSite(NetworkSite site) {
        sitesHandler.remove(site);
    }

    public void clearSites() {
        sitesHandler.clear();
    }

    public void open(int tcpPort, int websocketPort) throws InterruptedException {
        if (bossGroup != null && !bossGroup.isTerminated()) {
            return;
        }
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        SetupContext context = new SetupContext(
            bossGroup,
            workerGroup,
            sessionRegistry,
            messageRegistry,
            this::receivedMessage
        );
        tcpChannel = TcpServerSetup.init(context, tcpPort);
        wsChannel = WebsocketServerSetup.init(context, websocketPort);
        InetSocketAddress localAddress = (InetSocketAddress) tcpChannel.localAddress();
        ip = localAddress.getAddress().getHostAddress();
        open = true;
    }

    public void close() {
        lobbyHandler.closeAll();
        open = false;
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        tcpChannel.close().syncUninterruptibly();
        wsChannel.close().syncUninterruptibly();
    }

    public void sendToAll(Message message) {
        if (!open) {
            return;
        }
        int type = messageRegistry.getType(message);
        byte[] data = MessageUtils.toMessageBytes(type, message);
        sessionRegistry.all().forEach(it -> it.send(data));
    }

    private void receivedMessage(ClientConnection connection, Message message) {
        sitesHandler.pass(connection, message);
    }

    public SharedData getSharedData() {
        return sharedData;
    }
}
