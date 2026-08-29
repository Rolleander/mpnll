package com.broll.mpnll.server;

import com.broll.mpnll.ConnectionDefaults;

public class ServerLauncher {

    public static void main(String[] args) {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : ConnectionDefaults.DEFAULT_TCP_PORT;
        int websocketPort = args.length > 1 ? Integer.parseInt(args[1]) : ConnectionDefaults.DEFAULT_WS_PORT;
        MpnllServer server = new MpnllServer();
        server.open(tcpPort, websocketPort);
    }
}
