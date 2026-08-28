package com.broll.mpnll.server;

import com.broll.mpnll.NtLobbyMessagesRegistry;

public class ServerLauncher {

    public static void main(String[] args) throws InterruptedException {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int websocketPort = args.length > 1 ? Integer.parseInt(args[1]) : 8081;
        MpnllServer server = new MpnllServer();
        server.registerMessages(NtLobbyMessagesRegistry::register);
        server.open(tcpPort, websocketPort);
    }
}
