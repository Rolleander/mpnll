package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.ClientOperation;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;
import com.broll.mpnll.nt.NT_ListLobbies;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_ServerInformation;

public class ListLobbiesOperation extends ClientOperation<LobbyLookup> {

    private String ip;

    public ListLobbiesOperation() {
        this(null);
    }

    public ListLobbiesOperation(String ip) {
        this.ip = ip;
    }

    @Override
    protected void operation() {
        if (ip == null) {
            requireConnected();
        } else {
            connect(ip);
        }
        register(new ClientSite() {
            @Override
            protected void registerReceivers(MessageReceiverRegistry registry) {
                registry.connect(NT_ServerInformation.newBuilder(), this::receivedInfo);
                registry.connect(NT_LobbyReconnected.newBuilder(), this::receivedReconnect);
                registry.connect(NT_LobbyNoJoin.newBuilder(), this::receivedError);
            }

            private void receivedInfo(NT_ServerInformation info) {
                String ip = getConnectedIp();

            }

            private void receivedReconnect(NT_LobbyReconnected reconnected) {

            }

            private void receivedError(NT_LobbyNoJoin error) {
                fail(new Exception("Could not list lobbies: " + error.getReason()));
            }
        });

        send(
            NT_ListLobbies.newBuilder()
                .setAuthenticationKey(getClientAuthentication().getKey())
                .setVersion(getClientVersion()).build()
        );
    }

}
