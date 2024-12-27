package com.broll.mpnll.server.site;

import com.broll.mpnll.server.MpnllServer;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.lobby.LobbyHandler;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.utils.AnnotationScanner;
import com.broll.mpnll.server.utils.Autoshared;
import com.broll.mpnll.server.utils.SharedField;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class NetworkSite {

    private final static Logger Log = LoggerFactory.getLogger(NetworkSite.class);
    private LobbyHandler lobbyHandler;
    private MpnllServer server;
    private ClientConnection connection;
    private List<SharedField> sharedFields = new ArrayList<>();

    public void init(MpnllServer server, LobbyHandler lobbyHandler) {
        this.server = server;
        this.lobbyHandler = lobbyHandler;
        this.scanSharedFields();
    }

    private void scanSharedFields() {
        AnnotationScanner.findAnnotatedFields(this, Autoshared.class).forEach(finding ->
            initSharedField(finding.getLeft(), finding.getRight())
        );
    }

    private void initSharedField(Field field, Autoshared shared) {
        field.setAccessible(true);
        SharedField sharedField = new SharedField(
            this,
            field,
            shared.value()
        );
        sharedFields.add(sharedField);
    }

    public void receive(ClientConnection connection, Message message) {
        this.connection = connection;
        sharedFields.forEach(it -> it.inject(server, connection));
    }

    public <T extends NetworkSite> T accessSite(Class<T> siteClass) {
        T site = (T) server.getSiteInstances(connection).get(siteClass);
        site.receive(connection, null);
        return site;
    }

    protected MpnllServer getServer() {
        return server;
    }

    protected LobbyHandler getLobbyHandler() {
        return lobbyHandler;
    }

    protected ClientConnection getConnection() {
        return connection;
    }

    protected void respond(Message message) {
        connection.send(message);
    }

    protected User getUser() {
        return connection.getUser();
    }

    protected Lobby getLobby() {
        User user = getUser();
        if (user != null) {
            return user.getLobby();
        }
        return null;
    }

}
